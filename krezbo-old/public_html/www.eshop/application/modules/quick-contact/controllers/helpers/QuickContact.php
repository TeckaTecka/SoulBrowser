<?php
class Zend_Controller_Action_Helper_QuickContact extends Zend_Controller_Action_Helper_Abstract
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
    		->prependStylesheet('/css/modules/quick-contact/eshop.css');
        
        $this->view->jQuery()->addOnLoad(
       		'jQuery("#quick-contact-bar").click(
       			function(){
					jQuery("#quick-contact-box").slideToggleWidth();
    		 	}
    		 );
    		 if ((getWidth()) >= 1400){
    		 	jQuery("#quick-contact-box").slideRight();
    		 }else{
    		 	jQuery("#quick-contact-box").slideLeft();
    		 };');
        
        $settingsTab = new QuickContact_Model_DbTable_Settings();
        $quickContact = $settingsTab->getFlag('quick-contact');
        $this->view->quickContactEmail = $quickContact['email'];
    }
	public function Admin()
    {
    	
    }
}