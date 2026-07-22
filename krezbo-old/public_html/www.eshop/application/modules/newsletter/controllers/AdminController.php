<?php

class Newsletter_AdminController extends Zend_Controller_Action
{
	public function init()
    {
    	$auth = Zend_Auth::getInstance();
    	$user = $auth->getIdentity();
	    if(($auth->hasIdentity()) AND ($user['type'] == 'Admin')){
	    	$this->_helper->layout()->setLayout('admin');
    		$this->_helper->admin->initLayout();
    		
    		$this->view->addScriptPath(APPLICATION_PATH .'/modules/newsletter/views/scripts');
	    }else{
	    	$this->_helper->redirector->gotoRoute(array(), 'eshop_index_index');
	    }
    }
	public function indexAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/modules/newsletter/admin.css')
    		->prependStylesheet('/css/shared/table.css')
    		->prependStylesheet('/css/shared/tabs.css')
    		->prependStylesheet('/css/shared/paginator.css');
        
    	$this->view->headScript()
        	->appendFile('/js/jquery.tools.min.js');
        
        $this->view->jQuery()->addOnLoad(
       		'$("td span[title], td span a[title]").tooltip({
        		effect: "fade",
        		position: "center right",
    			opacity: 0.8,
    			predelay: 500
    		 });
    		 $("#newsletters").click(function(){
        		$("#tab1").css({display: "block"});$("#newsletters").addClass("active");
    		 	$("#tab2").css({display: "none"});$("#consumers").removeClass("active");
    		 });
    		 $("#consumers").click(function(){
    			$("#tab1").css({display: "none"});$("#newsletters").removeClass("active");
    			$("#tab2").css({display: "block"});$("#consumers").addClass("active");
    		 });');
        
        // NEWSLETTERS ****************************************************************************
        $page = $this->_getParam('page');
        
    	$db = Zend_Registry::get('db');
    	$adapter = new Zend_Paginator_Adapter_DbSelect(
    		$db->select()
    			->from('newsletters')
    	);
    	$paginator = new Zend_Paginator($adapter);
    	$paginator->setItemCountPerPage(20)
    		->setCurrentPageNumber($page);
    	//Zend_Debug::dump($paginator);
    	$this->view->paginator = $paginator;
    	
    	$newslettersTab = new Newsletter_Model_DbTable_Newsletters();
    	$newsletters = $newslettersTab->getNewsletters($page);
    	//Zend_Debug::dump($newsletters);
    	$this->view->newsletters = $newsletters;
    	// ****************************************************************************************
    	
    	// CONSUMERS ******************************************************************************
    	$db = Zend_Registry::get('db');
    	$adapter = new Zend_Paginator_Adapter_DbSelect(
    		$db->select()
    			->from('newsletters_emails')
    	);
    	$paginator2 = new Zend_Paginator($adapter);
    	$paginator2->setItemCountPerPage(20)
    		->setCurrentPageNumber($page);
    	//Zend_Debug::dump($paginator);
    	$this->view->paginator2 = $paginator2;
    	
    	$newslettersEmailsTab = new Newsletter_Model_DbTable_NewslettersEmails();
    	$emails = $newslettersEmailsTab->getEmails($page);
    	$this->view->emails = $emails;
    	// ****************************************************************************************
    	
    }
    public function addAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/modules/newsletter/admin.css')
    		->prependStylesheet('/css/shared/tabs.css')
    		->prependStylesheet('/css/shared/form.css')
    		->prependStylesheet('/css/cleditor/jquery.cleditor.css');
    		
    	$this->view->headScript()
        	->appendFile('/js/jquery.tools.min.js')
			->appendFile('/js/cleditor/jquery.cleditor.min.js')
			->appendFile('/js/cleditor/jquery.cleditor.table.min.js')
			->appendFile('/js/cleditor/jquery.cleditor.icon.min.js');
        $this->view->jQuery()->addOnLoad(
        	'$("form :input.tool-tip").tooltip({
        		effect: "fade",
        		offset: [20, 100],
        		opacity: 0.8
    		 });
    		 $("#newsletter").cleditor({
    		 	width:"478",
    		 	height:"300"
    		 });');
        
    		
    	$form = new Newsletter_Form_Add();
    	$form->setAction($this->view->url(array(), 'newsletter_admin_add'));
    	$form->setAttrib('id', 'form-newsletter-add');
    	$this->view->form = $form;
    	
    	if (($this->getRequest()->isPost()) AND ($this->getRequest()->getPost('newsletterSave')))
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    		
	    	if ($form->isValid($formData))
	    	{
	    		$newslettersTab = new Newsletter_Model_DbTable_Newsletters();
	    		$id = $newslettersTab->setNewsletter(
	    			$formData['title'],
	    			$formData['newsletter']
	    		);
	    		
	    		$this->_helper->redirector->gotoRoute(array('id'	=>	$id), 'newsletter_admin_edit');
	    	}
    	}
    }
    public function editAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/modules/newsletter/admin.css')
    		->prependStylesheet('/css/shared/tabs.css')
    		->prependStylesheet('/css/shared/form.css')
    		->prependStylesheet('/css/cleditor/jquery.cleditor.css');
    		
    	$this->view->headScript()
        	->appendFile('/js/jquery.tools.min.js')
			->appendFile('/js/cleditor/jquery.cleditor.min.js')
			->appendFile('/js/cleditor/jquery.cleditor.table.min.js')
			->appendFile('/js/cleditor/jquery.cleditor.icon.min.js');
        $this->view->jQuery()->addOnLoad(
        	'$("form :input.tool-tip").tooltip({
        		effect: "fade",
        		offset: [20, 100],
        		opacity: 0.8
    		 });
    		 $("#newsletter").cleditor({
    		 	width:"478",
    		 	height:"300"
    		 });
    		 $("#newsletters").click(function(){
        		$("#tab1").css({display: "block"});$("#newsletters").addClass("active");
    		 	$("#tab2").css({display: "none"});$("#send").removeClass("active");
    		 });
    		 $("#send").click(function(){
    			$("#tab1").css({display: "none"});$("#newsletters").removeClass("active");
    			$("#tab2").css({display: "block"});$("#send").addClass("active");
    		 });');
        
    	$id = $this->_getParam('id');
    		
		// FORM NEWSLETTER ************************************************************************
    	$form = new Newsletter_Form_Add();
    	$form->setAction($this->view->url(array(), 'newsletter_admin_edit'));
    	$form->setAttrib('id', 'form-newsletter-add');
    	$this->view->form = $form;
    	
    	$newslettersTab = new Newsletter_Model_DbTable_Newsletters();
    	$newsletter = $newslettersTab->getNewsletter($id);
		$data = array(
			'title'			=>	$newsletter['title'],
			'newsletter'	=>	$newsletter['newsletter']
		);
    	$form->populate($data);
	    // ****************************************************************************************
    	// FORM SEND ******************************************************************************
    	$formSend = new Newsletter_Form_Send();
    	$formSend->setAction($this->view->url(array(), 'newsletter_admin_edit'));
    	$formSend->setAttrib('id', 'form-newsletter-send');
    	$this->view->formSend = $formSend;
    	// ****************************************************************************************
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    	// FORM NEWSLETTER ************************************************************************
    		$save = $this->getRequest()->getPost('newsletterSave');
	    	if ($save)
	    	{
		    	if ($form->isValid($formData))
		    	{
		    		$newslettersTab->updateNewsletter(
		    			$id,
		    			$formData['title'],
		    			$formData['newsletter']
		    		);
		    	}
	    	}
	    	// ****************************************************************************************
	    	// FORM SEND ******************************************************************************
	    	$send = $this->getRequest()->getPost('sendNewsletters');
	    	if ($send)
	    	{
		    	if ($formSend->isValid($formData))
		    	{
		    		$newslettersTab->setSent(
		    			$id,
		    			1
		    		);
		    		
		    		$newsletter = $newslettersTab->getNewsletter($id);
		    		
		    		// Poslat email
	                $settingsTab = new Newsletter_Model_DbTable_Settings();
		            $smtp = $settingsTab->getFlag('smtp');
		            //Zend_Debug::dump($smtp);
		            $config = array('auth'		=>	'login',
					  				'username'	=>	$smtp['username'],
					   				'password'	=>	$smtp['password'],
					   				'ssl'		=>	$smtp['ssl'],
					   				'port'		=>	$smtp['port']);
					$transport = new Zend_Mail_Transport_Smtp($smtp['smtp'], $config);
					$eshop = $settingsTab->getFlag('eshop');
					$mail = new Zend_Mail('UTF-8');
					$mail->setHeaderEncoding(Zend_Mime::ENCODING_BASE64);
					$mail->setBodyHtml($newsletter['newsletter'],'UTF-8', 'UTF-8');
					$mail->setFrom($eshop['email'], $eshop['title']);
					$mail->setSubject('Novinky - '.$eshop['title']);
					
					$newslettersEmailsTab = new Newsletter_Model_DbTable_NewslettersEmails();
					$emails = $newslettersEmailsTab->getAllEmails();
					
					foreach ($emails as $email) {
						$mail->addTo($email['email'], $email['email']);
						$mail->send($transport);
					}
				}
	    	}
	    	// ****************************************************************************************
    	}
    }
    public function delAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css')
    		->prependStylesheet('/css/shared/tabs.css');
    	
    	$id = $this->_getParam('id');
    	
    	$form = new Newsletter_Form_Del();
    	$form->setAction($this->view->url(array('id' => $id), 'newsletter_admin_del'));
    	$this->view->form = $form;
    	
    	$newslettersTab = new Newsletter_Model_DbTable_Newsletters();
	    $newsletter = $newslettersTab->getNewsletter($id);
    	$this->view->newsletter = $newsletter['title'];
    	
    	if ($this->getRequest()->isPost()) {
            $delete = $this->getRequest()->getPost('delete');
                        
            if ($delete != NULL) {
            	$newslettersTab->setFlag($id, 'delete');
          	}
            $this->_helper->redirector->gotoRoute(array(), 'newsletter_admin_index');
    	}
    }
    public function delConsumerAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css')
    		->prependStylesheet('/css/shared/tabs.css');
    	
    	$id = $this->_getParam('id');
    	
    	$form = new Newsletter_Form_Del();
    	$form->setAction($this->view->url(array('id' => $id), 'newsletter_admin_del-consumer'));
    	$this->view->form = $form;
    	
    	$newslettersEmailsTab = new Newsletter_Model_DbTable_NewslettersEmails();
	    $email = $newslettersEmailsTab->getEmail($id);
    	$this->view->email = $email['email'];
    	
    	if ($this->getRequest()->isPost()) {
            $delete = $this->getRequest()->getPost('delete');
                        
            if ($delete != NULL) {
            	$newslettersEmailsTab->setFlag($id, 'delete');
          	}
            $this->_helper->redirector->gotoRoute(array(), 'newsletter_admin_index');
    	}
    }
}