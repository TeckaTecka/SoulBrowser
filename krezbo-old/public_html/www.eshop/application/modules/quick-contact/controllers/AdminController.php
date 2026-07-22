<?php

class QuickContact_AdminController extends Zend_Controller_Action
{
	public function init()
    {
    	$auth = Zend_Auth::getInstance();
    	$user = $auth->getIdentity();
	    if(($auth->hasIdentity()) AND ($user['type'] == 'Admin')){
	    	$this->_helper->layout()->setLayout('admin');
    		$this->_helper->admin->initLayout();
    		
    		$this->view->addScriptPath(APPLICATION_PATH .'/modules/quick-contact/views/scripts');
	    }else{
	    	$this->_helper->redirector->gotoRoute(array(), 'eshop_index_index');
	    }
    }
	public function indexAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/modules/quick-contact/admin.css')
    		->prependStylesheet('/css/shared/form.css')
    		->prependStylesheet('/css/shared/tabs.css');
        
        $this->view->jQuery()->addOnLoad(
       		'$("form :input.tool-tip").tooltip({
        		effect: "fade",
        		position: "center right",
    			opacity: 0.8,
    			predelay: 500
    		 });');
        
    	$form = new QuickContact_Form_QuickContact();
    	$form->setAction($this->view->url(array(), 'quick-contact_admin_index'));
    	$form->setAttrib('id', 'form-quick-contact');
    	$this->view->form = $form;
    	
    	$settingsTab = new QuickContact_Model_DbTable_Settings();
    	
    	if (($this->getRequest()->isPost()) AND ($this->getRequest()->getPost('quickContactSend')))
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    		
	    	if ($form->isValid($formData))
	    	{
    			$settingsTab->updateFlag('quick-contact', 'title', $formData['title']);
	    		$settingsTab->updateFlag('quick-contact', 'email', $formData['email']);
	    		$this->_helper->redirector->gotoRoute(array(), 'quick-contact_admin_saved');
	    	}
        }else{
        	$quickContact = $settingsTab->getFlag('quick-contact');
        	$data = array(
				'title'			=>	$quickContact['title'],
				'email'			=>	$quickContact['email']
			);
			$form->populate($data);
        }
    }
    public function savedAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/modules/quick-contact/admin.css')
    		->prependStylesheet('/css/shared/form.css')
    		->prependStylesheet('/css/shared/tabs.css');
    }
}