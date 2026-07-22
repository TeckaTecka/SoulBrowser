<?php

class Admin_Options_ConsumptionsController extends Zend_Controller_Action
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
    		
    	$consumptionsTab = new Admin_Model_DbTable_Consumptions();
    	$consumptions = $consumptionsTab->getConsumptions();
    	$this->view->consumptions = $consumptions;
    	
    	$settingsTab = new Admin_Model_DbTable_Settings();
    	$currency = $settingsTab->getFlag('eshop');
    	$this->view->currency = $currency['currency'];
    }
    public function addAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css');
    	
    	$formConsumptions = new Admin_Form_Options_Consumptions_Consumptions();
    	$formConsumptions->setAction($this->view->url(array(), 'admin_options_consumptions-add'));
    	$formConsumptions->setAttrib('id', 'form-add-consumption');
    	$this->view->formConsumptions = $formConsumptions;
    	$settingsTab = new Admin_Model_DbTable_Settings();
    	$currency = $settingsTab->getFlag('eshop');
    	$formConsumptions->price->setDescription($currency['currency']);
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    	if ($formConsumptions->isValid($formData))
	    	{
	    		$consumptionsTab = new Admin_Model_DbTable_Consumptions();
	    		
	    		$id = $consumptionsTab->setConsuption(
	    			$formData['consumption'],
	    			$formData['price'],
		    		0
		    	);
	    	
    			$this->_helper->redirector->gotoRoute(array('id'	=>	$id), 'admin_options_consumptions-edit');
	    	}
    	}
    }
	public function editAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css');
        $this->view->jQuery()->addOnLoad(
        	'$("#consumptions").click(function(){
    		 	$("#tab1").css({display: "block"});$("#consumptions").addClass("active");
    			$("#tab2").css({display: "none"});$("#settings").removeClass("active");
    		 });
    		 $("#settings").click(function(){
    			$("#tab1").css({display: "none"});$("#consumptions").removeClass("active");
    			$("#tab2").css({display: "block"});$("#settings").addClass("active");
    		 });
			');
        
    	$id = $this->_getParam('id');
    	
    	// FORM CONSUMPTIONS **********************************************************************    	
    	$formConsumptions = new Admin_Form_Options_Consumptions_Consumptions();
    	$formConsumptions->setAction($this->view->url(array(), 'admin_options_consumptions-edit'));
    	$formConsumptions->setAttrib('id', 'form-edit-consumption');
    	$this->view->formConsumptions = $formConsumptions;
    	$settingsTab = new Admin_Model_DbTable_Settings();
    	$currency = $settingsTab->getFlag('eshop');
    	$formConsumptions->price->setDescription($currency['currency']);
    	// FORM SETTINGS **************************************************************************
    	$formSettings = new Admin_Form_Options_Consumptions_Settings();
    	$formSettings->setAction($this->view->url(array(), 'admin_options_consumptions-edit'));
    	$formSettings->setAttrib('id', 'form-edit-settings');
    	$this->view->formSettings = $formSettings;
    	
    	$consumptionsTab = new Admin_Model_DbTable_Consumptions();
    	$consumption = $consumptionsTab->getConsumption($id);
		$data = array(
			'consumption'	=>	$consumption['consumption'],
			'price'			=>	$consumption['price'],
			'show'			=>	$consumption['show']
		);
    	$formConsumptions->populate($data);
    	$formSettings->populate($data);
    		
    	/*****************************************************************************************/	
    	
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
    		// FORM CONSUMPTIONS ******************************************************************
    		$saveConsumption = $this->getRequest()->getPost('saveConsumption');
    		if ($saveConsumption)
    		{
    			if ($formConsumptions->isValid($formData))
    			{
    				$consumptionsTab->updateConsumption(
	    				$id,
	    				$formData['consumption'],
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
    				$consumptionsTab->updateConsumption(
	    				$id,
	    				$data['consumption'],
	    				$formData['show']
	    			);
    			}
    		}
    	}
    }
	public function delAction()
    {
    	$id = $this->_getParam('id');
    	
    	$form = new Admin_Form_Options_Consumptions_Del();
    	$form->setAction($this->view->url(array('id' => $id), 'admin_options_consumptions-del'));
    	$this->view->form = $form;
    	
    	$consumptionsTab = new Admin_Model_DbTable_Consumptions();
    	$consumption = $consumptionsTab->getConsumption($id);
    	$this->view->consumption = $consumption['consumption'];
    	
    	if ($this->getRequest()->isPost()) {
            $delete = $this->getRequest()->getPost('delete');
                        
            if ($delete != NULL) {
            	$consumptionsTab->delConsumption($id);
          	}
            $this->_helper->redirector->gotoRoute(array(), 'admin_options_consumptions-index');
    	}
    	
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css');
    }
}