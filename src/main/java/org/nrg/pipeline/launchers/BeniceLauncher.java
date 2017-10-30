/*
 * org.nrg.pipeline.launchers.Freesurfer53Launcher
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 9/5/13 2:38 PM
 */

package org.nrg.pipeline.launchers;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.pipeline.XnatPipelineLauncher;
import org.nrg.pipeline.utils.PipelineFileUtils;
import org.nrg.pipeline.xmlbeans.ParameterData;
import org.nrg.pipeline.xmlbeans.ParameterData.Values;
import org.nrg.pipeline.xmlbeans.ParametersDocument.Parameters;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;

public class BeniceLauncher extends PipelineLauncher{
    static org.apache.log4j.Logger logger = Logger.getLogger(BeniceLauncher.class);

    public BeniceLauncher() {}

    // Need to override this abstract method in PipelineLauncher
    public boolean launch(RunData data, Context context) {
        try {
            ItemI data_item = TurbineUtils.GetItemBySearch(data);
            XnatImagesessiondata image = new XnatImagesessiondata(data_item);
            XnatPipelineLauncher xnatPipelineLauncher = XnatPipelineLauncher.GetLauncher(data, context, image);

            String pipelineLocation = ((String)TurbineUtils.GetPassedParameter("pipeline_location",data));

            xnatPipelineLauncher.setPipelineName(pipelineLocation);
            xnatPipelineLauncher.setSupressNotification(true);

            String buildDir = PipelineFileUtils.getBuildDir(image.getProject(), true);
            buildDir +=  "fsrfer" ;

            xnatPipelineLauncher.setBuildDir(buildDir);
            xnatPipelineLauncher.setNeedsBuildDir(false);

            Parameters parameters = Parameters.Factory.newInstance();
            ParameterData param;

            String[] stringParams = new String[] {"recon_all_args","format"};
            for (String paramName : stringParams) {
                if (TurbineUtils.HasPassedParameter(paramName, data)) {
                    param = parameters.addNewParameter();
                    param.setName(paramName);
                    param.addNewValues().setUnique((String) TurbineUtils.GetPassedParameter(paramName, data));
                }
            }

            // Add scan list
            HashMap<String,String> scanParams = new HashMap<String,String>();
            scanParams.put("scan","scan_ids");
            scanParams.put("t2","T2_ids");
            scanParams.put("flair","FLAIR_ids");
            for (Map.Entry<String, String> scanParam : scanParams.entrySet()) {
                ArrayList<String> selectedScans = getCheckBoxSelections(data, scanParam.getKey());
                if (selectedScans != null && !selectedScans.isEmpty()) {
                    param = parameters.addNewParameter();
                    param.setName(scanParam.getValue());
                    Values value = param.addNewValues();
                    for (String scanId : selectedScans) {
                        value.addList(scanId);
                    }
                }
            }

            String emailsCSV = XDAT.getUserDetails().getEmail() + "," + XDAT.getSiteConfigPreferences().getAdminEmail();
            for (String list : new String[] {"general-notif-list", "specific-notif-list"}) {
                if (TurbineUtils.HasPassedParameter(list, data)) {
                    String notifCSV = (String) TurbineUtils.GetPassedParameter(list, data);
                    for (String email : notifCSV.split(",")) {
                        if (StringUtils.isNotBlank(email) &&
                                !(email.contains("'") || email.contains("\"") || emailsCSV.contains(email))) {
                            emailsCSV += "," + email;
                        }
                    }
                }
            }
            for (String email : emailsCSV.split(",")) {
                xnatPipelineLauncher.notify(email);
            }
            param = parameters.addNewParameter();
            param.setName("email_csv_in");
            param.addNewValues().setUnique(emailsCSV);


            String paramFileName = getName(pipelineLocation);
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            String s = formatter.format(date);

            paramFileName += "_params_" + s + ".xml";

            String paramFilePath = saveParameters(buildDir + File.separator + image.getLabel(),paramFileName,parameters);

            xnatPipelineLauncher.setParameterFile(paramFilePath);

            return xnatPipelineLauncher.launch();
        }catch(Exception e) {
            logger.error(e.getCause() + " " + e.getLocalizedMessage());
            return false;
        }
    }

}
