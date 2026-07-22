<?php

class Auth_AddressController extends Zend_Controller_Action
{
	protected $user;
	
	public function init()
    {
    	$auth = Zend_Auth::getInstance();
    	if(!$auth->hasIdentity()){
    		$this->_helper->redirector->gotoRoute(array(), 'auth_index_login');
    	}else{
    		$this->_helper->layout()->setLayout('eshop');
        	$this->_helper->eshop->initLayout();
        	$this->user = $auth->getIdentity();
    	}        
    }
	public function indexAction()
    {
    	$this->view->headLink()->prependStylesheet('/css/table.css');
    	
    	$this->view->headScript()->appendFile('/js/jquery.tools.min.js');
    	
    	$this->view->jQuery()->addOnLoad(
    		'$("td span a[title]").tooltip({
    			effect: "fade",
    			position: "center center",
    			opacity: 0.8,
    			predelay: 700
    		 });
		');
    	
    	$addressTab = new Auth_Model_DbTable_Addresses();
    	$addresses = $addressTab->getAddressesByUserId($this->user['id']);
    	$this->view->addresses = $addresses;
    	//Zend_Debug::dump($addresses);
    	
    	
    }
	public function addAction()
    {
    	$this->view->headLink()->prependStylesheet('/css/form.css');
    	
    	$this->view->jQuery()->addOnLoad(
    		'$("div.radions #addressType-1").click(function(){
    			$("div.person").css({display: "block"});
    			$("div.company").css({display: "none"});
    		 });
    		 $("div.radions #addressType-2").click(function(){
    		 	$("div.person").css({display: "none"});
    			$("div.company").css({display: "block"});
    		 });'
    	);
    	
    	$form = new Auth_Form_Address_Add();
    	$form->setAction($this->view->url(array(),'auth_address_add'));
    	$this->view->form = $form;
    	$countriesTab = new Auth_Model_DbTable_Countries();
    	$countries = $countriesTab->getCountries();
    	for ($i = 0; $i < count($countries); $i++) {
    		$options[$countries[$i]['id']] = $countries[$i]['country'];
    	}
    	$form->country->setMultiOptions($options);
    	
    	if ($this->getRequest()->isPost() AND (($this->getRequest()->getPost('addressAddStorno')) OR ($this->getRequest()->getPost('addressAddSubmit')))){
    		$storno = $this->getRequest()->getPost('addressAddStorno');
    		if ($storno == NULL) {
	    		$formData = $this->getRequest()->getPost();
	    		//Zend_Debug::dump($formData);
	    		
    			if ($formData['addressType']=='1'){
	    			$form->companyName->setRequired(false);
	    			$form->companyIC->setRequired(false);
	    			
	    			$formData['companyName'] = NULL;
	    			$formData['companyIC'] = NULL;
	    			$formData['companyDIC'] = NULL;
	    		}else{
    				$this->view->jQuery()->addOnLoad(
    					'$("#auth div.person").css({display: "none"});
    					 $("#auth div.company").css({display: "block"});');
	    			$form->personName->setRequired(false);
	    			$form->personSurname->setRequired(false);
	    			
	    			$formData['personTitle'] = NULL;
	    			$formData['personName'] = NULL;
	    			$formData['personSurname'] = NULL;
	    		}
	    		
	    		if ($form->isValid($formData)){
	    			$addressesTab = new Auth_Model_DbTable_Addresses();
	    			$address_id = $addressesTab->setAddress(
	    				$formData['street'], $formData['street_nr'], $formData['city'], $formData['zip'], $formData['country'],
	    				$formData['contactPerson'], $formData['contactEmail'], $formData['contactPhone'],
	    				$formData['personTitle'], $formData['personName'], $formData['personSurname'],
	    				$formData['companyName'], $formData['companyIC'], $formData['companyDIC']);

	    			$users2addressTab = new Auth_Model_DbTable_Users2addresses();
	    			$users2addressTab->setUsers2addresses($this->user['id'], $address_id);
	    			
	    			$this->_helper->redirector->gotoRoute(array(), 'auth_address_index');
	    		}
    		}else{
    			$this->_helper->redirector->gotoRoute(array(), 'auth_address_index');
    		}
    	}
    }
    public function delAction()
    {
    	$this->view->headLink()->prependStylesheet('/css/form.css');
    	
    	$form = new Auth_Form_Address_Del();
    	$form->setAction($this->view->url(array(),'auth_address_del'));
    	$this->view->form = $form;
    	
    	if ($this->getRequest()->isPost() AND (($this->getRequest()->getPost('addressDelStorno')) OR ($this->getRequest()->getPost('addressDelSubmit')))){
    		$storno = $this->getRequest()->getPost('addressDelStorno');
    		if ($storno == NULL) {
    			// DELETE
    			$id = $this->_getParam('id');
    			$addressesTab = new Auth_Model_DbTable_Addresses();
    			$addressesTab->setFlag($id, 'delete');
    			
    			$this->_helper->redirector->gotoRoute(array(), 'auth_address_index');
    		}else{// STORNO
    			$this->_helper->redirector->gotoRoute(array(), 'auth_address_index');
    		}
    	}
   	}
}