<?php

class Zend_Controller_Action_Helper_Admin extends Zend_Controller_Action_Helper_Abstract
{
	protected $view;
	
	public function __construct() {
		$viewRenderer = Zend_Controller_Action_HelperBroker::getStaticHelper('viewRenderer');
		$this->view = $viewRenderer->view;
	}
    
    public function initLayout()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/admin/layout.css')
    		->prependStylesheet('/css/shared/tabs.css');
    	$this->view->appName = $_SERVER["SERVER_NAME"];
    	
    	// MODUL AUTH *********************
        //Zend_Controller_Action_HelperBroker::getStaticHelper('Auth')->Auth();        
        // ********************************
        // MODUL QUICK-CONTACT ********************************************************************
        //try {
        //	Zend_Controller_Action_HelperBroker::getStaticHelper('QuickContact')->Admin();
        //} catch (Exception $e) {
        //}
        // ****************************************************************************************
        $this->view->headScript()
        	->appendFile('/js/jquery/jquery.tools.min.js');
        $this->view->jQuery()->addOnLoad(
        	// footer panel **************************************************
			/*'$("#footer-open").click(function(){
			 	$(".footer-body").slideDown("slow");});
				$("#footer-close").click(function(){
					$(".footer-body").slideUp("slow");});
				$("#footer-toggle div").click(function(){
					$("#footer-toggle div").toggle();});'
				.*/
			// ***************************************************************
			// Header menu tooltip *******************************************
			   '$("header .menu a[title]").tooltip({
			   		effect: "fade",
    				position: "bottom left",
    				opacity: 0.8,
    				delay: 0,
    				predelay: 500
    			});'
    		// ***************************************************************
			);
    }
}