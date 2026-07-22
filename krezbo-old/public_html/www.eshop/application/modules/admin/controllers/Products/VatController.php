<?php

class Admin_Products_VatController extends Zend_Controller_Action
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
    	
    	$vatTab = new Admin_Model_DbTable_Vat();
    	$vats = $vatTab->getVats();
    	$this->view->vats = $vats;
    	//Zend_Debug::dump($vats);
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
        
    	// FORM DPH *******************************************************************************
        $formVat = new Admin_Form_Products_Vat_Vat();
    	$formVat->setAction($this->view->url(array(), 'admin_products_vat-add'));
    	$formVat->setAttrib('id', 'form-add-vat');
    	$this->view->formVat = $formVat;
    	
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    		
	    	if ($formVat->isValid($formData))
	    	{
    			$vatTab = new Admin_Model_DbTable_Vat();
    			$id = $vatTab->setVat($formData['vat']);
    			
	    		$this->_helper->redirector->gotoRoute(array(), 'admin_products_vat-index');
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
    	
    	// FORM DPH *******************************************************************************
        $formVat = new Admin_Form_Products_Vat_Vat();
    	$formVat->setAction($this->view->url(array(), 'admin_products_vat-edit'));
    	$formVat->setAttrib('id', 'form-add-vat');
    	$this->view->formVat = $formVat;
    	/*****************************************************************************************/
		$vatTab = new Admin_Model_DbTable_Vat();
		
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    	
			// FORM VAT ***********************************************************************
	    	$saveVat = $this->getRequest()->getPost('saveVat');
	    	if ($saveVat)
	    	{
	    		if ($formVat->isValid($formData))
	    		{
	    			$vatTab->updateVat(
	    				$id,
	    				$formData['vat']
			    	);
	    		}
	    		
	    		$this->_helper->redirector->gotoRoute(array(), 'admin_products_vat-index');
	    	}
    	}else{
	    	$vat = $vatTab->getVat($id);
	    	$data = array(
	    		'vat'	=>	$vat['vat']
	    	);
    		$formVat->populate($data);
	    	}
	}
	public function delAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css');
    	
    	$id = $this->_getParam('id');
    	
    	$form = new Admin_Form_Products_Vat_Del();
    	$form->setAction($this->view->url(array('id' => $id), 'admin_products_vat-del'));
    	$this->view->form = $form;
    	
    	$vatTab = new Admin_Model_DbTable_Vat();
    	
    	$vat = $vatTab->getVat($id);
    	$this->view->vat = $vat['title'];
    	
    	if ($this->getRequest()->isPost()) {
            $delete = $this->getRequest()->getPost('delete');
                        
            if ($delete != NULL) {
            	$vatTab->setFlag($id, 'delete');
          	}
            $this->_helper->redirector->gotoRoute(array(), 'admin_products_vat-index');
    	}
    }
}