package com.catify.processengine.core.data.dataobjects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import com.catify.processengine.core.processdefinition.jaxb.TCatchEvent;
import com.catify.processengine.core.processdefinition.jaxb.TDataObject;
import com.catify.processengine.core.processdefinition.jaxb.TDataObjectReference;
import com.catify.processengine.core.processdefinition.jaxb.TFlowElement;
import com.catify.processengine.core.processdefinition.jaxb.TFlowNode;
import com.catify.processengine.core.processdefinition.jaxb.TReceiveTask;
import com.catify.processengine.core.processdefinition.jaxb.TSendTask;
import com.catify.processengine.core.processdefinition.jaxb.TServiceTask;
import com.catify.processengine.core.processdefinition.jaxb.TSubProcess;
import com.catify.processengine.core.processdefinition.jaxb.TTask;
import com.catify.processengine.core.processdefinition.jaxb.TThrowEvent;

public class DataObjectIdService {
	
	private DataObjectIdService() {
		
	}
	
	public static final String DATAOUTPUTOBJECTID = "dataOutputObjectId";
	public static final String DATAINPUTOBJECTID = "dataInputObjectId";
	
	/**
	 * Get data object ids from a list of jaxb flow elements (including sub process object ids).
	 *
	 * @param flowElementsJaxb the jaxb flow elements
	 * @return the data object ids
	 */
	public static Set<String> getAllDataObjectIds(List<JAXBElement<? extends TFlowElement>> flowElementsJaxb) {
		Set<String> dataObjectIdSet = new HashSet<String>();
		
		for (JAXBElement<? extends TFlowElement> flowElementJaxb : flowElementsJaxb) {

			if (flowElementJaxb.getValue() instanceof TFlowNode) {
				TFlowNode flowNodeJaxb = (TFlowNode) flowElementJaxb.getValue();

				Map<String, String> dataObjectIds = DataObjectIdService.getDataObjectIds(flowNodeJaxb);
				dataObjectIdSet.add(dataObjectIds.get(DataObjectIdService.DATAINPUTOBJECTID));
				dataObjectIdSet.add(dataObjectIds.get(DataObjectIdService.DATAOUTPUTOBJECTID));

				// get object ids of the sub process flow nodes
				if (flowNodeJaxb instanceof TSubProcess) {
					TSubProcess subProcessJaxb = (TSubProcess) flowNodeJaxb;
					dataObjectIdSet.addAll(getAllDataObjectIds(subProcessJaxb.getFlowElement()));
				}
			}
		}
		
		return dataObjectIdSet;
	}

	/**
	 * Gets the data object ids (input and output, if present) of a flow node.
	 *
	 * @param flowNodeJaxb the jaxb flow node
	 * @return the data object ids
	 */
	public static Map<String, String> getDataObjectIds(TFlowNode flowNodeJaxb) {
		
		// map to store ids of dataInput and/or dataOutput
		Map<String, String> objectIds = new HashMap<String, String>();

		// node is catching event
		if (flowNodeJaxb.getClass().getSuperclass().equals(TCatchEvent.class)) {

			// get data object id associated to this node (if any)
			if (((TCatchEvent) flowNodeJaxb).getDataOutputAssociation().size() > 0) {
				// TODO: only one data object supported, see redmine #96 (minor
				// task)
				TDataObjectReference dataObjectReferenceJaxb = (TDataObjectReference) ((TCatchEvent) flowNodeJaxb)
						.getDataOutputAssociation().get(0).getTargetRef();
				TDataObject dataObjectJaxb = (TDataObject) dataObjectReferenceJaxb
						.getDataObjectRef();
				objectIds.put(DATAOUTPUTOBJECTID, dataObjectJaxb.getId());
			}
			return objectIds;

		// node is throwing event
		} else if (flowNodeJaxb.getClass().getSuperclass()
				.equals(TThrowEvent.class)) {

			// get data object id associated to this node (if any)
			if (((TThrowEvent) flowNodeJaxb).getDataInputAssociation().size() > 0) {
				// TODO: only one data object supported, see redmine #96 (minor
				// task)
				TDataObjectReference dataObjectReferenceJaxb = (TDataObjectReference) ((TThrowEvent) flowNodeJaxb)
						.getDataInputAssociation().get(0).getTargetRef();
				TDataObject dataObjectJaxb = (TDataObject) dataObjectReferenceJaxb
						.getDataObjectRef();
				objectIds.put(DATAINPUTOBJECTID, dataObjectJaxb.getId());
			}
			return objectIds;

		// node is a task
		} else if (flowNodeJaxb.getClass().getSuperclass().equals(TTask.class)) {

			// get data object id associated to this node (if any)
			if (((TTask) flowNodeJaxb).getDataInputAssociation().size() > 0
					|| ((TTask) flowNodeJaxb).getDataOutputAssociation().size() > 0) {
				// TODO: only one data object supported, see redmine #96 (minor
				// task)

				// every task must be handled in its special way
				if (flowNodeJaxb.getClass().equals(TReceiveTask.class)) {
					TDataObjectReference dataObjectReferenceJaxb = (TDataObjectReference) ((TReceiveTask) flowNodeJaxb)
							.getDataOutputAssociation().get(0).getTargetRef();
					TDataObject dataObjectJaxb = (TDataObject) dataObjectReferenceJaxb
							.getDataObjectRef();
					objectIds.put(DATAOUTPUTOBJECTID, dataObjectJaxb.getId());

				} else if (flowNodeJaxb.getClass().equals(TSendTask.class)) {
					TDataObjectReference dataObjectReferenceJaxb = (TDataObjectReference) ((TSendTask) flowNodeJaxb)
							.getDataInputAssociation().get(0).getTargetRef();
					TDataObject dataObjectJaxb = (TDataObject) dataObjectReferenceJaxb
							.getDataObjectRef();
					objectIds.put(DATAINPUTOBJECTID, dataObjectJaxb.getId());
				}

				if (flowNodeJaxb.getClass().equals(TServiceTask.class)) {
					TDataObjectReference dataObjectReferenceJaxb = (TDataObjectReference) ((TServiceTask) flowNodeJaxb)
							.getDataOutputAssociation().get(0).getTargetRef();
					TDataObject dataObjectJaxb = (TDataObject) dataObjectReferenceJaxb
							.getDataObjectRef();
					objectIds.put(DATAOUTPUTOBJECTID, dataObjectJaxb.getId());

					dataObjectReferenceJaxb = (TDataObjectReference) ((TServiceTask) flowNodeJaxb)
							.getDataInputAssociation().get(0).getTargetRef();
					dataObjectJaxb = (TDataObject) dataObjectReferenceJaxb
							.getDataObjectRef();
					objectIds.put(DATAINPUTOBJECTID, dataObjectJaxb.getId());
				}
			}
			return objectIds;

		// if type is not implemented yet return empty map
		} else {
			return objectIds;
		}
	}
	
}