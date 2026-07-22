<?php

class Admin_Options_PaymentsController extends Zend_Controller_Action
{
	public function init()
    {
    	$auth = Zend_Auth::getInstance();
    	$user = $auth->getIdentity();
	    if(($auth->hasIdentity()) AND ($user['type'] == 'Admin')){
	    	$this->_helper->layout()->setLayout('admin');
    		$this->_helper->admin->initLayout();
	    }else{
	    	$this->_helper->redirector->gotoRoute(array(), 'eshop_index_index');
	    }
    }
    public function indexAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/table.css');
    		
    	$paymentsTab = new Admin_Model_DbTable_Payments();
    	$payments = $paymentsTab->getPayments();
    	$this->view->payments = $payments;
    	
    	$settingsTab = new Admin_Model_DbTable_Settings();
    	$currency = $settingsTab->getFlag('eshop');
    	$this->view->currency = $currency['currency'];
    }
    public function addAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css');
    	
    	$formPayments = new Admin_Form_Options_Payments_Payments();
    	$formPayments->setAction($this->view->url(array(), 'admin_options_payments-add'));
    	$formPayments->setAttrib('id', 'form-payments');
    	$this->view->formPayments = $formPayments;
    	$settingsTab = new Admin_Model_DbTable_Settings();
    	$currency = $settingsTab->getFlag('eshop');
    	$formPayments->price->setDescription($currency['currency']);
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    	if ($formPayments->isValid($formData))
	    	{
	    		$paymentsTab = new Admin_Model_DbTable_Payments();
	    		
	    		$id = $paymentsTab->setPayment(
	    			$formData['payment'],
	    			$formData['price'],
		    		0
		    	);
	    		
    			$this->_helper->redirector->gotoRoute(array('id'	=>	$id), 'admin_options_payments-edit');
	    	}
    	}
    }
	public function editAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css');
        $this->view->jQuery()->addOnLoad(
        	'$("#payment").click(function(){
    		 	$("#tab1").css({display: "block"});$("#payment").addClass("active");
    			$("#tab2").css({display: "none"});$("#settings").removeClass("active");
    		 });
    		 $("#settings").click(function(){
    			$("#tab1").css({display: "none"});$("#payment").removeClass("active");
    			$("#tab2").css({display: "block"});$("#settings").addClass("active");
    		 });
			');
        
    	$id = $this->_getParam('id');
    	
    	// FORM PAYMENTS **************************************************************************
    	$formPayments = new Admin_Form_Options_Payments_Payments();
    	$formPayments->setAction($this->view->url(array(), 'admin_options_payments-edit'));
    	$formPayments->setAttrib('id', 'form-payments');
    	$this->view->formPayments = $formPayments;
    	$settingsTab = new Admin_Model_DbTable_Settings();
    	$currency = $settingsTab->getFlag('eshop');
    	$formPayments->price->setDescription($currency['currency']);
    	// FORM SETTINGS **************************************************************************
    	$formSettings = new Admin_Form_Options_Payments_Settings();
    	$formSettings->setAction($this->view->url(array(), 'admin_options_payments-edit'));
    	$formSettings->setAttrib('id', 'form-settings');
    	$this->view->formSettings = $formSettings;
    	
    	$paymentsTab = new Admin_Model_DbTable_Payments();
    	$payment = $paymentsTab->getPayment($id);
		$data = array(
			'payment'	=>	$payment['payment'],
			'price'		=>	$payment['price'],
			'show'		=>	$payment['show']
		);
    	$formPayments->populate($data);
    	$formSettings->populate($data);
    	
    	//*****************************************************************************************
    	
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
    		// FORM PAYMENTS *********************************************************************
    		$savePayments = $this->getRequest()->getPost('savePayments');
    		if ($savePayments)
    		{
    			if ($formPayments->isValid($formData))
    			{
    				$paymentsTab->updatePayment(
	    				$id,
	    				$formData['payment'],
	    				$formData['price'],
	    				$data['show']
	    			);
    			}
    		}
    		// FORM SETTINGS ******************************************************************
    		$saveSettings = $this->getRequest()->getPost('saveSettings');
    		if ($saveSettings)
    		{
    			if ($formSettings->isValid($formData))
    			{
    				$paymentsTab->updatePayment(
	    				$id,
	    				$data['payment'],
	    				$formData['show']
	    			);
    			}
    		}
    	}
    }
	public function delAction()
    {
    	$id = $this->_getParam('id');
    	
    	$form = new Admin_Form_Options_Payments_Del();
    	$form->setAction($this->view->url(array('id' => $id), 'admin_options_payments-del'));
    	$this->view->form = $form;
    	
    	$paymentsTab = new Admin_Model_DbTable_Payments();
    	$payment = $paymentsTab->getPayment($id);
    	$this->view->payment = $payment['payment'];
    	
    	if ($this->getRequest()->isPost()) {
            $delete = $this->getRequest()->getPost('delete');
                        
            if ($delete != NULL) {
            	$paymentsTab->delPayment($id);
          	}
            $this->_helper->redirector->gotoRoute(array(), 'admin_options_payments-index');
    	}
    	
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css');
    }
}