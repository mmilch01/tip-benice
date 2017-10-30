package org.nrg.xnat.turbine.modules.screens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.model.ArcPipelinedataI;
import org.nrg.xdat.om.*;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xdat.turbine.utils.TurbineUtils;

@SuppressWarnings("unused")
public class PipelineScreen_Benice extends DefaultPipelineScreen {


    public final String SCANTYPE_CONFIG_TOOL_NAME = "params";
    public final String SCANTYPE_CONFIG_FILE_NAME = "mr_scan_types";
    public final String BOLD_CONFIG_TOOL_NAME = "params";
    public final String BOLD_CONFIG_FILE_NAME = "bold_scan_types";


    static Logger logger = Logger.getLogger(PipelineScreen_Benice.class);
    public void finalProcessing(RunData data, Context context){


        try {
            String pipelinePath = (String)TurbineUtils.GetPassedParameter("pipeline",data);
            String schemaType = om.getXSIType();
            ArcProject arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(project);

            if (schemaType.equals(XnatProjectdata.SCHEMA_ELEMENT_NAME)) {
                ArcProjectPipeline pipelineData = (ArcProjectPipeline)arcProject.getPipelineByPath(pipelinePath);
                context.put("pipeline", pipelineData);
                setParameters(pipelineData, context);
            }else {
                ArcPipelinedataI pipelineData = arcProject.getPipelineForDescendantByPath(schemaType, pipelinePath);
                context.put("pipeline", pipelineData);
                setParameters(pipelineData, context);
            }

            if (!(om instanceof XnatImagesessiondata)) {
                String m ="Cannot run Benice on a non-image session.";
                data.setMessage(m);
                throw new Exception(m);
            }

            ArrayList<XnatImagescandata> mrScans = getScansFromScantypeConfig(SCANTYPE_CONFIG_TOOL_NAME, SCANTYPE_CONFIG_FILE_NAME);
            ArrayList<XnatImagescandata> boldScans = getScansFromScantypeConfig(BOLD_CONFIG_TOOL_NAME, BOLD_CONFIG_FILE_NAME);

            context.put("mrScans", mrScans.size()>0 ? mrScans : ((XnatImagesessiondata) om).getScans_scan());
            context.put("boldScans", boldScans.size()>0 ? boldScans : ((XnatImagesessiondata) om).getScans_scan());

            ArrayList<String> nonAdvancedParamsArray = Lists.newArrayList("fstd", "mprs", "subject");
            HashMap<String,String> advancedParams = Maps.newHashMap();
            for (Map.Entry<String,ArcPipelineparameterdata> entry : projectParameters.entrySet()) {
                String key = entry.getKey();
                ArcPipelineparameterdata value = entry.getValue();
                if (!nonAdvancedParamsArray.contains(key)) {
                    if (value.getCsvvalues()==null) {
                        advancedParams.put(key,"");
                    } else {
                        advancedParams.put(key,value.getCsvvalues());
                    }
                }
            }
            context.put("advancedParams",advancedParams);

            context.put("projectParameters", projectParameters);

        }catch(Exception e) {
            logger.error("An error occurred", e);
            e.printStackTrace();
        }
    }
}
