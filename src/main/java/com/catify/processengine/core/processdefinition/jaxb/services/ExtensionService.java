package com.catify.processengine.core.processdefinition.jaxb.services;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import com.catify.processengine.core.processdefinition.jaxb.TFlowNode;
import com.catify.processengine.core.processdefinition.jaxb.TMessageEventDefinition;
import com.catify.processengine.core.processdefinition.jaxb.TMessageIntegration;
import com.catify.processengine.core.processdefinition.jaxb.TNOfM;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TVersion;


public class ExtensionService {
	
	private ExtensionService() {
	
	}
	
	public static TVersion getTVersion(TProcess processJaxb) {
		if (processJaxb.getExtensionElements() != null) {
			for (Object extensionElement : processJaxb.getExtensionElements().getAny()) {
				if (extensionElement instanceof JAXBElement && (((JAXBElement<?>) extensionElement).getValue() instanceof TVersion) ) {
	
					JAXBElement<?> jaxbElement = (JAXBElement<?>) extensionElement;
					TVersion versionJaxb = (TVersion) ((JAXBElement<?>) jaxbElement).getValue();
	
					return versionJaxb;
				}
			}
		}
		return null; 
	}
	
	public static TMessageIntegration getTMessageIntegration(TFlowNode flowNodeJaxb) {
		if (flowNodeJaxb.getExtensionElements() != null) {
			for (Object extensionElement : flowNodeJaxb.getExtensionElements().getAny()) {
				if (extensionElement instanceof JAXBElement && (((JAXBElement<?>) extensionElement).getValue() instanceof TMessageIntegration) ) {
	
					JAXBElement<?> jaxbElement = (JAXBElement<?>) extensionElement;
					TMessageIntegration messageIntegrationJaxb = (TMessageIntegration) ((JAXBElement<?>) jaxbElement).getValue();
	
					return messageIntegrationJaxb;
				}
			}
		}
		return null; 
	}
	
	public static List<TMessageIntegration> getTMessageIntegrations(TFlowNode flowNodeJaxb) {
		List<TMessageIntegration> mI = new ArrayList<TMessageIntegration>();
		if (flowNodeJaxb.getExtensionElements() != null) {
			for (Object extensionElement : flowNodeJaxb.getExtensionElements().getAny()) {
				if (extensionElement instanceof JAXBElement && (((JAXBElement<?>) extensionElement).getValue() instanceof TMessageIntegration) ) {
	
					JAXBElement<?> jaxbElement = (JAXBElement<?>) extensionElement;
					TMessageIntegration messageIntegrationJaxb = (TMessageIntegration) ((JAXBElement<?>) jaxbElement).getValue();
	
					mI.add(messageIntegrationJaxb);
				}
			}
			return mI;
		}
		return null; 
	}
	
	public static TMessageIntegration getTMessageIntegration(TMessageEventDefinition messageEventDefinitionJaxb) {
		if (messageEventDefinitionJaxb.getExtensionElements() != null) {
			for (Object extensionElement : messageEventDefinitionJaxb.getExtensionElements().getAny()) {
				if (extensionElement instanceof JAXBElement && (((JAXBElement<?>) extensionElement).getValue() instanceof TMessageIntegration) ) {
	
					JAXBElement<?> jaxbElement = (JAXBElement<?>) extensionElement;
					TMessageIntegration messageIntegrationJaxb = (TMessageIntegration) ((JAXBElement<?>) jaxbElement).getValue();
	
					return messageIntegrationJaxb;
				}
			}
		}
		return null; 
	}

	
	public static TNOfM getTNOfM(TFlowNode flowNodeJaxb) {
		if (flowNodeJaxb.getExtensionElements() != null) {
			for (Object extensionElement : flowNodeJaxb.getExtensionElements().getAny()) {
				if (extensionElement instanceof JAXBElement && (((JAXBElement<?>) extensionElement).getValue() instanceof TNOfM) ) {
	
					JAXBElement<?> jaxbElement = (JAXBElement<?>) extensionElement;
					TNOfM nOfMJaxb = (TNOfM) ((JAXBElement<?>) jaxbElement).getValue();
	
					return nOfMJaxb;
				}
			}
		}
		return null; 
	}
	
}
