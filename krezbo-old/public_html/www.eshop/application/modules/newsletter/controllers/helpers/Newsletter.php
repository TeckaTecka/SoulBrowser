<?php
class Zend_Controller_Action_Helper_Newsletter extends Zend_Controller_Action_Helper_Abstract
{
	protected $front;
	protected $view;
	
    public function __construct() {
        $this->front = Zend_Controller_Front::getInstance();
        $viewRenderer = Zend_Controller_Action_HelperBroker::getStaticHelper('viewRenderer');
		$this->view = $viewRenderer->view;
	}
    
	public function Eshop()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/modules/newsletter/eshop.css')
    		->prependStylesheet('/css/shared/form.css');
        
    	//$this->view->headScript()
        //	->appendFile('/js/jquery.tools.min.js');
        
        $this->view->jQuery()->addOnLoad(
       		'jQuery("#newsletter-bar").click(
       			function(){
					jQuery("#newsletter-box").slideToggleWidth();
    		 	}
    		 );
    		 if ((getWidth()) >= 1400){
    		 	jQuery("#newsletter-box").slideRight();
    		 }else{
    		 	jQuery("#newsletter-box").slideLeft();
    		 };
    		 $("form#form-newsletter-add :input.tool-tip").tooltip({
        		effect: "fade",
        		offset: [-30, 50],
        		opacity: 0.8
    		 });');
    	
        $formNewsletter = new Newsletter_Form_AddEmail();
    	//$form->setAction($this->view->url(array(), 'quick-contact_admin_index'));
    	$formNewsletter->setAttrib('id', 'form-newsletter-add');
    	$this->view->formNewsletter = $formNewsletter;
    	
    	if (($this->getRequest()->isPost()) AND ($this->getRequest()->getPost('newsletterSave')))
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    		
	    	if ($formNewsletter->isValid($formData))
	    	{
	    		$newslettersEmailsTab = new Newsletter_Model_DbTable_NewslettersEmails();
	    		$newslettersEmailsTab->setEmail($formData['email']);
	    		
	    		$redirector = Zend_Controller_Action_HelperBroker::getStaticHelper('redirector');
				$redirector->gotoRoute(array(), 'newsletter_eshop_added');
	    		//$this->_helper->getHelper('redirector')->gotoRoute(array(), 'newsletter_eshop_added');
	    	}
    	}
    }
	public function Admin()
    {
    	
    }
}