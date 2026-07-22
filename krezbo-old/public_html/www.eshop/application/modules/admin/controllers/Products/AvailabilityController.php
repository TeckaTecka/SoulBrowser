<?php

class Admin_Products_AvailabilityController extends Zend_Controller_Action
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
    	$this->view->jQuery()->addOnLoad(
    		'$("td span[title], td span a[title]").tooltip({
    			effect: "fade",
    			position: "top center",
    			opacity: 0.8,
    			predelay: 1000
    		 });');
    	
    	$availabilityTab = new Admin_Model_DbTable_Availability();
    	$availability = $availabilityTab->getAvailabilityAll();
    	$this->view->availability = $availability;
    	//Zend_Debug::dump($availability);
    }
    public function addAction()
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
    		 });');
        
    	// FORM AVAILABILITY **********************************************************************
        $formAvailability = new Admin_Form_Products_Availability_Availability();
    	$formAvailability->setAction($this->view->url(array(), 'admin_products_availability-add'));
    	$formAvailability->setAttrib('id', 'form-add-availability');
    	$this->view->formAvailability = $formAvailability;
    	
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    		
	    	if ($formAvailability->isValid($formData))
	    	{
    			$availabilityTab = new Admin_Model_DbTable_Availability();
    			$id = $availabilityTab->setAvailability($formData['availability']);
    			
	    		$this->_helper->redirector->gotoRoute(array(), 'admin_products_availability-index');
	    	}
        }
    }
	public function editAction()
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
    		 });');
        
    	$id = $this->_getParam('id');
    	
    	// FORM AVAILABILITY **********************************************************************
        $formAvailability = new Admin_Form_Products_Availability_Availability();
    	$formAvailability->setAction($this->view->url(array(), 'admin_products_availability-edit'));
    	$formAvailability->setAttrib('id', 'form-edit-availability');
    	$this->view->formAvailability = $formAvailability;
    	/*****************************************************************************************/
		$availabilityTab = new Admin_Model_DbTable_Availability();
		
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    	
			// FORM VAT ***********************************************************************
	    	$saveAvailability = $this->getRequest()->getPost('saveAvailability');
	    	if ($saveAvailability)
	    	{
	    		if ($formAvailability->isValid($formData))
	    		{
	    			$availabilityTab->updateAvailability(
	    				$id,
	    				$formData['availability']
			    	);
	    		}
	    		
	    		$this->_helper->redirector->gotoRoute(array(), 'admin_products_availability-index');
	    	}
    	}else{
	    	$availability = $availabilityTab->getAvailability($id);
	    	$data = array(
	    		'availability'	=>	$availability['title']
	    	);
    		$formAvailability->populate($data);
	    	}
	}
	public function delAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css');
    	
    	$id = $this->_getParam('id');
    	
    	$form = new Admin_Form_Products_Availability_Del();
    	$form->setAction($this->view->url(array('id' => $id), 'admin_products_availability-del'));
    	$this->view->form = $form;
    	
    	$availabilityTab = new Admin_Model_DbTable_Availability();
    	
    	$availability = $availabilityTab->getAvailability($id);
    	$this->view->availability = $availability['title'];
    	
    	if ($this->getRequest()->isPost()) {
            $delete = $this->getRequest()->getPost('delete');
                        
            if ($delete != NULL) {
            	$availabilityTab->setFlag($id, 'delete');
          	}
            $this->_helper->redirector->gotoRoute(array(), 'admin_products_availability-index');
    	}
    }
}