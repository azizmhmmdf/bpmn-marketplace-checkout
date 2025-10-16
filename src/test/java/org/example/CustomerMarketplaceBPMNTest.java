package org.example;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;

import org.camunda.bpm.model.bpmn.instance.Process;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CustomerMarketplaceBPMNTest {

    @Test
    public void testCreateTwoPoolCollaboration() {
        BpmnModelInstance modelInstance = Bpmn.createEmptyModel();

        Definitions definitions = modelInstance.newInstance(Definitions.class);
        definitions.setTargetNamespace("http://camunda.org/schema/1.0/bpmn");
        definitions.setId("Definitions_1");
        modelInstance.setDefinitions(definitions);

        Collaboration collaboration = modelInstance.newInstance(Collaboration.class);
        collaboration.setId("collaboration_1");
        collaboration.setName("Customer-Marketplace");
        definitions.addChildElement(collaboration);

        Process customerProcess = modelInstance.newInstance(Process.class);
        customerProcess.setId("customerProcess");
        customerProcess.setExecutable(true);
        definitions.addChildElement(customerProcess);

        Participant customerParticipant = modelInstance.newInstance(Participant.class);
        customerParticipant.setId("pool1");
        customerParticipant.setName("Customer");
        customerParticipant.setProcess(customerProcess);
        collaboration.addChildElement(customerParticipant);

        StartEvent customerStart = createNode(modelInstance, customerProcess, StartEvent.class, "custStart", "Checkout Initiated");
        UserTask fillDetails = createNode(modelInstance, customerProcess, UserTask.class, "fillDetails", "Fill Checkout\nDetails");
        UserTask confirmOrder = createNode(modelInstance, customerProcess, UserTask.class, "confirmOrder", "Confirm Order");
        ExclusiveGateway customerGateway = createNode(modelInstance, customerProcess, ExclusiveGateway.class, "customerGateway", "");

        IntermediateCatchEvent receiveConfirm = createNode(modelInstance, customerProcess, IntermediateCatchEvent.class, "receiveConfirm", "Receive Confirmation\n& Shipment Details");
        EndEvent customerEndSuccess = createNode(modelInstance, customerProcess, EndEvent.class, "custEndSuccess", "");

        IntermediateCatchEvent receiveFailure = createNode(modelInstance, customerProcess, IntermediateCatchEvent.class, "receiveFailure", "Receive Failure\nNotification");
        EndEvent customerEndFailure = createNode(modelInstance, customerProcess, EndEvent.class, "custEndFailure", "");

        connectNodes(modelInstance, customerProcess, customerStart, fillDetails);
        connectNodes(modelInstance, customerProcess, fillDetails, confirmOrder);
        connectNodes(modelInstance, customerProcess, confirmOrder, customerGateway);

        connectNodesWithCondition(modelInstance, customerProcess, customerGateway, receiveConfirm, "true");
        connectNodes(modelInstance, customerProcess, receiveConfirm, customerEndSuccess);

        connectNodesWithCondition(modelInstance, customerProcess, customerGateway, receiveFailure, "false");
        connectNodes(modelInstance, customerProcess, receiveFailure, customerEndFailure);

        Process marketplaceProcess = modelInstance.newInstance(Process.class);
        marketplaceProcess.setId("marketplaceProcess");
        marketplaceProcess.setExecutable(true);
        definitions.addChildElement(marketplaceProcess);

        Participant marketplaceParticipant = modelInstance.newInstance(Participant.class);
        marketplaceParticipant.setId("pool2");
        marketplaceParticipant.setName("Marketplace System");
        marketplaceParticipant.setProcess(marketplaceProcess);
        collaboration.addChildElement(marketplaceParticipant);

        StartEvent mkStart = createNode(modelInstance, marketplaceProcess, StartEvent.class, "mkStart", "Receive Order\nRequest");
        ServiceTask processPayment = createNode(modelInstance, marketplaceProcess, ServiceTask.class, "processPayment", "Process\nPayment");
        ExclusiveGateway paymentGateway = createNode(modelInstance, marketplaceProcess, ExclusiveGateway.class, "paymentGateway", "Payment\nSuccessful?");

        ServiceTask sendFailure = createNode(modelInstance, marketplaceProcess, ServiceTask.class, "sendFailure", "Send Failure\nNotification");
        EndEvent failureEnd = createNode(modelInstance, marketplaceProcess, EndEvent.class, "failureEnd", "");

        ServiceTask updateInventory = createNode(modelInstance, marketplaceProcess, ServiceTask.class, "updateInventory", "Update Stock");
        ServiceTask confirmOrderService = createNode(modelInstance, marketplaceProcess, ServiceTask.class, "confirmOrderService", "Confirm Order");
        ServiceTask sendConfirmation = createNode(modelInstance, marketplaceProcess, ServiceTask.class, "sendConfirmation", "Send Confirmation\nMessage");
        ServiceTask arrangeShipment = createNode(modelInstance, marketplaceProcess, ServiceTask.class, "arrangeShipment", "Arrange\nShipment");
        EndEvent successEnd = createNode(modelInstance, marketplaceProcess, EndEvent.class, "successEnd", "");

        connectNodes(modelInstance, marketplaceProcess, mkStart, processPayment);
        connectNodes(modelInstance, marketplaceProcess, processPayment, paymentGateway);

        connectNodesWithCondition(modelInstance, marketplaceProcess, paymentGateway, sendFailure, "false");
        connectNodes(modelInstance, marketplaceProcess, sendFailure, failureEnd);

        connectNodesWithCondition(modelInstance, marketplaceProcess, paymentGateway, updateInventory, "true");
        connectNodes(modelInstance, marketplaceProcess, updateInventory, confirmOrderService);
        connectNodes(modelInstance, marketplaceProcess, confirmOrderService, sendConfirmation);
        connectNodes(modelInstance, marketplaceProcess, sendConfirmation, arrangeShipment);
        connectNodes(modelInstance, marketplaceProcess, arrangeShipment, successEnd);

        File file = new File("customerMarketplace.bpmn");
        file.getParentFile().mkdirs();
        Bpmn.writeModelToFile(file, modelInstance);

        System.out.println("BPMN file created: " + file.getAbsolutePath());

        assertNotNull(modelInstance);
        assertEquals(2, collaboration.getParticipants().size());
    }

    private <T extends FlowNode> T createNode(BpmnModelInstance modelInstance, Process process, Class<T> type, String id, String name) {
        T node = modelInstance.newInstance(type);
        node.setId(id);
        node.setName(name);
        process.addChildElement(node);
        return node;
    }

    private void connectNodes(BpmnModelInstance modelInstance, Process process, FlowNode source, FlowNode target) {
        SequenceFlow flow = modelInstance.newInstance(SequenceFlow.class);
        flow.setId(source.getId() + "_to_" + target.getId());
        flow.setSource(source);
        flow.setTarget(target);
        process.addChildElement(flow);
        source.getOutgoing().add(flow);
        target.getIncoming().add(flow);
    }

    private void connectNodesWithCondition(BpmnModelInstance modelInstance, Process process, FlowNode source, FlowNode target, String condition) {
        SequenceFlow flow = modelInstance.newInstance(SequenceFlow.class);
        flow.setId(source.getId() + "_to_" + target.getId());
        flow.setSource(source);
        flow.setTarget(target);

        ConditionExpression condExpr = modelInstance.newInstance(ConditionExpression.class);
        condExpr.setTextContent("true".equals(condition) ? "${paymentSuccess == true}" : "${paymentSuccess == false}");
        flow.setConditionExpression(condExpr);

        process.addChildElement(flow);
        source.getOutgoing().add(flow);
        target.getIncoming().add(flow);
    }
}