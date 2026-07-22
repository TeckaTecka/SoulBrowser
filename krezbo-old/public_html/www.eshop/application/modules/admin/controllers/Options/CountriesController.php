<?php

class Admin_Options_CountriesController extends Zend_Controller_Action
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
    	$countriesTab = new Admin_Model_DbTable_Countries();
    	$countries = $countriesTab->getCountries();
    	$this->view->countries = $countries;
    	
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/table.css');
    }
    public function addAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css');
    	
    	$formCountries = new Admin_Form_Options_Countries_Countries();
    	$formCountries->setAction($this->view->url(array(), 'admin_options_countries-add'));
    	$formCountries->setAttrib('id', 'form-add');
    	$this->view->formCountries = $formCountries;
    	
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    	if ($formCountries->isValid($formData))
	    	{
	    		$countriesTab = new Admin_Model_DbTable_Countries();
	    		
	    		$id = $countriesTab->setCountry(
	    			$formData['country'],
		    		0
		    	);
	    		
    			$this->_helper->redirector->gotoRoute(array('id'	=>	$id), 'admin_options_countries-edit');
	    	}
    	}
    }
	public function editAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css');
        $this->view->jQuery()->addOnLoad(
        	'$("#country").click(function(){
    		 	$("#tab1").css({display: "block"});$("#country").addClass("active");
    			$("#tab2").css({display: "none"});$("#settings").removeClass("active");
    		 });
    		 $("#settings").click(function(){
    			$("#tab1").css({display: "none"});$("#country").removeClass("active");
    			$("#tab2").css({display: "block"});$("#settings").addClass("active");
    		 });
			');
        
    	$id = $this->_getParam('id');
    	
    	// FORM COUNTRIES *************************************************************************
    	$formCountries = new Admin_Form_Options_Countries_Countries();
    	$formCountries->setAction($this->view->url(array(), 'admin_options_countries-edit'));
    	$formCountries->setAttrib('id', 'form-add-countries');
    	$this->view->formCountries = $formCountries;
    	
    	// FORM SETTINGS **************************************************************************
    	$formSettings = new Admin_Form_Options_Countries_Settings();
    	$formSettings->setAction($this->view->url(array(), 'admin_options_countries-edit'));
    	$formSettings->setAttrib('id', 'form-add-settings');
    	$this->view->formSettings = $formSettings;
    	
    	$countriesTab = new Admin_Model_DbTable_Countries();
    	$country = $countriesTab->getCountry($id);
		$data = array(
			'country'	=>	$country['country'],
			'show'		=>	$country['show']
		);
    	$formCountries->populate($data);
    	$formSettings->populate($data);
    	
    	/*****************************************************************************************/
    	
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
    		// FORM COUNTRIES *****************************************************************
    		$saveCountries = $this->getRequest()->getPost('saveCountries');
    		if ($saveCountries)
    		{
    			if ($formCountries->isValid($formData))
    			{
    				$countriesTab->updateCountry(
	    				$id,
	    				$formData['country'],
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
    				$countriesTab->updateCountry(
	    				$id,
	    				$data['country'],
	    				$formData['show']
	    			);
    			}
    		}
    	}
    }
	public function delAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css');
    	
    	$id = $this->_getParam('id');
    	
    	$form = new Admin_Form_Options_Countries_Del();
    	$form->setAction($this->view->url(array('id' => $id), 'admin_options_countries-del'));
    	$this->view->form = $form;
    	
    	$countriesTab = new Admin_Model_DbTable_Countries();
    	$country = $countriesTab->getCountry($id);
    	$this->view->country = $country['country'];
    	
    	if ($this->getRequest()->isPost()) {
            $delete = $this->getRequest()->getPost('delete');
                        
            if ($delete != NULL) {
            	$countriesTab->delCountry($id);
          	}
            $this->_helper->redirector->gotoRoute(array(), 'admin_options_countries-index');
    	}
    }
}