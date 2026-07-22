<?php

class Admin_Options_OptionsController extends Zend_Controller_Action
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
	public function eshopAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css');
    	$this->view->headScript()
        	->appendFile('/js/jquery.tools.min.js');
    	$this->view->jQuery()->addOnLoad(
        	'$("form :input.tool-tip").tooltip({
        		effect: "fade",
        		offset: [20, 100],
        		opacity: 0.8
    		 });
    		 $("#eshop").click(function(){
    		 	$("#tab1").css({display: "block"});$("#eshop").addClass("active");
    			$("#tab2").css({display: "none"});$("#currency").removeClass("active");
    			$("#tab3").css({display: "none"});$("#smtp").removeClass("active");
    			$("#tab4").css({display: "none"});$("#bank").removeClass("active");
    			$("#tab5").css({display: "none"});$("#company").removeClass("active");
    		 });
    		 $("#currency").click(function(){
    		 	$("#tab1").css({display: "none"});$("#eshop").removeClass("active");
    			$("#tab2").css({display: "block"});$("#currency").addClass("active");
    			$("#tab3").css({display: "none"});$("#smtp").removeClass("active");
    			$("#tab4").css({display: "none"});$("#bank").removeClass("active");
    			$("#tab5").css({display: "none"});$("#company").removeClass("active");
    		 });
    		 $("#smtp").click(function(){
    		 	$("#tab1").css({display: "none"});$("#eshop").removeClass("active");
    			$("#tab2").css({display: "none"});$("#currency").removeClass("active");
    			$("#tab3").css({display: "block"});$("#smtp").addClass("active");
    			$("#tab4").css({display: "none"});$("#bank").removeClass("active");
    			$("#tab5").css({display: "none"});$("#company").removeClass("active");
    		 });
    		 $("#bank").click(function(){
    		 	$("#tab1").css({display: "none"});$("#eshop").removeClass("active");
    			$("#tab2").css({display: "none"});$("#currency").removeClass("active");
    			$("#tab3").css({display: "none"});$("#smtp").removeClass("active");
    			$("#tab4").css({display: "block"});$("#bank").addClass("active");
    			$("#tab5").css({display: "none"});$("#company").removeClass("active");
    		 });
    		 $("#company").click(function(){
    		 	$("#tab1").css({display: "none"});$("#eshop").removeClass("active");
    			$("#tab2").css({display: "none"});$("#currency").removeClass("active");
    			$("#tab3").css({display: "none"});$("#smtp").removeClass("active");
    			$("#tab4").css({display: "none"});$("#bank").removeClass("active");
    			$("#tab5").css({display: "block"});$("#company").addClass("active");
    		 });
    	');
    	$settingsTab = new Admin_Model_DbTable_Settings();
    	// ESHOP **********************************************************************************
    	$eshopForm = new Admin_Form_Options_Eshop_Eshop();
    	$this->view->eshopForm = $eshopForm;
    	//Zend_Debug::dump($this->getRequest()->getPost('saveEshop'));
    	if ($this->getRequest()->isPost() AND $this->getRequest()->getPost('saveEshop'))
    	{
    		$formData = $this->getRequest()->getPost();
    		//Zend_Debug::dump($formData);
    		if ($eshopForm->isValid($formData)){
	    		$settingsTab->updateFlag('eshop', 'title', $formData['title']);
	    		$settingsTab->updateFlag('eshop', 'url', $formData['url']);
	    		$settingsTab->updateFlag('eshop', 'email', $formData['email']);
	    		$settingsTab->updateFlag('eshop', 'street', $formData['street']);
	    		$settingsTab->updateFlag('eshop', 'street_nr', $formData['street_nr']);
	    		$settingsTab->updateFlag('eshop', 'city', $formData['city']);
	    		$settingsTab->updateFlag('eshop', 'zip', $formData['zip']);
	    		$settingsTab->updateFlag('eshop', 'country', $formData['country']);
	    		$settingsTab->updateFlag('eshop', 'ic', $formData['companyIC']);
	    		$settingsTab->updateFlag('eshop', 'dic', $formData['companyDIC']);
	    		$settingsTab->updateFlag('eshop', 'phone', $formData['phone']);
	    		$settingsTab->updateFlag('eshop', 'mobile', $formData['mobile']);
	    		$settingsTab->updateFlag('eshop', 'fax', $formData['fax']);
    		}
    	}
    	
    	$eshop = $settingsTab->getFlag('eshop');
    	$data = array(
    		'title'			=>	$eshop['title'],
    		'url'			=>	$eshop['url'],
    		'email'			=>	$eshop['email'],
    		'street'		=>	$eshop['street'],
    		'street_nr'		=>	$eshop['street_nr'],
    		'city'			=>	$eshop['city'],
    		'zip'			=>	$eshop['zip'],
    		'country'		=>	$eshop['country'],
    		'companyIC'		=>	$eshop['ic'],
    		'companyDIC'	=>	$eshop['dic'],
    		'phone'			=>	$eshop['phone'],
    		'mobile'		=>	$eshop['mobile'],
    		'fax'			=>	$eshop['fax']
    	);
    	$eshopForm->populate($data);
    	// ****************************************************************************************
    	// CURRENCY *******************************************************************************
    	$currencyForm = new Admin_Form_Options_Eshop_Currency();
    	$this->view->currencyForm = $currencyForm;
    	
    	if ($this->getRequest()->isPost() AND $this->getRequest()->getPost('saveCurrency'))
    	{
    		$this->view->jQuery()->addOnLoad('$("#currency").click();');
    		$formData = $this->getRequest()->getPost();
    		//Zend_Debug::dump($formData);
    		if ($currencyForm->isValid($formData)){
	    		$settingsTab->updateFlag('eshop', 'currency', $formData['currency']);
    		}
    	}
    	
    	$currency = $settingsTab->getFlag('eshop');
    	$data = array(
    		'currency'	=>	$currency['currency']
    	);
    	$currencyForm->populate($data);
    	// ****************************************************************************************
    	// SMTP ***********************************************************************************
    	$smtpForm = new Admin_Form_Options_Eshop_Smtp();
    	$this->view->smtpForm = $smtpForm;
    	
    	if ($this->getRequest()->isPost() AND $this->getRequest()->getPost('saveSMTP'))
    	{
    		$this->view->jQuery()->addOnLoad('$("#smtp").click();');
    		$formData = $this->getRequest()->getPost();
    		//Zend_Debug::dump($formData);
    		if ($smtpForm->isValid($formData)){
	    		$settingsTab->updateFlag('smtp', 'username', $formData['username']);
	    		$settingsTab->updateFlag('smtp', 'password', $formData['password']);
	    		$settingsTab->updateFlag('smtp', 'ssl', $formData['ssl']);
	    		$settingsTab->updateFlag('smtp', 'port', $formData['port']);
	    		$settingsTab->updateFlag('smtp', 'smtp', $formData['smtp']);
    		}
    	}
    	
    	$smtp = $settingsTab->getFlag('smtp');
    	$data = array(
    		'username'	=>	$smtp['username'],
    		'password'	=>	$smtp['password'],
    		'ssl'		=>	$smtp['ssl'],
    		'port'		=>	$smtp['port'],
    		'smtp'		=>	$smtp['smtp']
    	);
    	$smtpForm->populate($data);
    	// ****************************************************************************************
    	// BANK ***********************************************************************************
    	$bankForm = new Admin_Form_Options_Eshop_Bank();
    	$this->view->bankForm = $bankForm;
    	
    	if ($this->getRequest()->isPost() AND $this->getRequest()->getPost('saveBank'))
    	{
    		$this->view->jQuery()->addOnLoad('$("#bank").click();');
    		$formData = $this->getRequest()->getPost();
    		//Zend_Debug::dump($formData);
    		if ($bankForm->isValid($formData)){
	    		$settingsTab->updateFlag('bank', 'account', $formData['account']);
	    		$settingsTab->updateFlag('bank', 'title', $formData['title']);
	    		$settingsTab->updateFlag('bank', 'iban', $formData['iban']);
	    		$settingsTab->updateFlag('bank', 'bic', $formData['bic']);
	    	}
    	}
    	
    	$bank = $settingsTab->getFlag('bank');
    	$data = array(
    		'account'	=>	$bank['account'],
    		'title'		=>	$bank['title'],
    		'iban'		=>	$bank['iban'],
    		'bic'		=>	$bank['bic']
    	);
    	$bankForm->populate($data);
    	// ****************************************************************************************
    	// COMPANY ********************************************************************************
    	$companyForm = new Admin_Form_Options_Eshop_Company();
    	$this->view->companyForm = $companyForm;
    	
    	if ($this->getRequest()->isPost() AND $this->getRequest()->getPost('saveCompany'))
    	{
    		$this->view->jQuery()->addOnLoad('$("#company").click();');
    		$formData = $this->getRequest()->getPost();
    		//Zend_Debug::dump($formData);
    		if ($companyForm->isValid($formData)){
	    		$settingsTab->updateFlag('company', 'registration', $formData['registration']);
	    	}
    	}
    	
    	$bank = $settingsTab->getFlag('company');
    	$data = array(
    		'registration'	=>	$bank['registration']
    	);
    	$companyForm->populate($data);
    	// ****************************************************************************************
    }
    public function aboutAction()
    {
    	
    }
    
}