package org.nrg.xnat.workflow.listeners;

import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xft.event.entities.WorkflowStatusEvent;
import org.nrg.xnat.event.listeners.PipelineEmailHandlerAbst;

import java.util.Map;

public class BeniceEmailHandler extends PipelineEmailHandlerAbst {
    final static Logger logger = Logger.getLogger(BeniceEmailHandler.class);

    private final String PIPELINE_NAME = "benice/Benice.xml";
    private final String PIPELINE_NAME_PRETTY = "Benice";

    public void handleEvent(WorkflowStatusEvent e) {
        if (!(e.getWorkflow() instanceof WrkWorkflowdata)) {
            return;
        }
        WrkWorkflowdata wrk = (WrkWorkflowdata)e.getWorkflow();
        Map<String,Object> params = Maps.newHashMap();
        params.put("pipelineName",PIPELINE_NAME_PRETTY);
        if (completed(e)) {
            // standardPipelineEmailImpl(e, wrk, PIPELINE_NAME, "/screens/PipelineEmail_Benice_success.vm", "processed with "+PIPELINE_NAME_PRETTY, "processed.lst", params);
        } else if (failed(e)) {
            standardPipelineEmailImpl(e, wrk, PIPELINE_NAME, DEFAULT_TEMPLATE_FAILURE, DEFAULT_SUBJECT_FAILURE, "processed.lst", params);
        }
    }
}
