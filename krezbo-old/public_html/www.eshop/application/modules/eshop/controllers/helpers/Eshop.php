<?php

class Zend_Controller_Action_Helper_Eshop extends Zend_Controller_Action_Helper_Abstract
{
	protected $view;
	
	public function __construct() {
        $viewRenderer = Zend_Controller_Action_HelperBroker::getStaticHelper('viewRenderer');
		$this->view = $viewRenderer->view;
    }
    
    public function initLayout()
    {
    	$this->view->headLink()
        	->prependStylesheet('/css/layout.css');
        $this->view->headScript()
        	->appendFile('/js/scripts.js')
        	->appendFile('/js/jquery/jquery.placeheld.min.js');
        $this->view->jQuery()->addOnLoad(
        	'jQuery(function( $ ) {
        		$("input[placeholder]").placeHeld();
        	});'
        );
        
    	// FORM SEARCH ****************************************************************************
        $formSearch = new Eshop_Form_Search();
        $formSearch->setAction($this->view->url(array(), 'eshop_index_search'));
    	$formSearch->setAttrib('id', 'form-search');
    	$this->view->formSearch = $formSearch;
    	// ****************************************************************************************
    	
    	$categoriesTab = new Eshop_Model_DbTable_Categories();
    	$categories = $categoriesTab->getCategories();
    	$this->view->categories = $categories;
    	//Zend_Debug::dump($categories);
    	
    	$manufacrotersTab = new Eshop_Model_DbTable_Manufacturers();
    	$manufacroters = $manufacrotersTab->getManufacturers();
    	$this->view->manufacroters = $manufacroters;
    	//Zend_Debug::dump($manufacroters);
    	
    	$pagesTab = new Eshop_Model_DbTable_Pages();
        $pages = $pagesTab->getPagesAll();
        $this->view->pages = $pages;
        //Zend_Debug::dump($pages);
    	
	    // MODUL CART *****************************************************************************
    	try {
        	Zend_Controller_Action_HelperBroker::getStaticHelper('Cart')->Cart();
    	} catch (Exception $e) {
        	
        }       
        // ****************************************************************************************
        // MODUL NEWSLETTER ***********************************************************************
        //try {
        //	Zend_Controller_Action_HelperBroker::getStaticHelper('Newsletter')->Eshop();
        //} catch (Exception $e) {
        	
        //}
        // ****************************************************************************************
        // MODUL QUICK-CONTACT ********************************************************************
        //try {
        //	Zend_Controller_Action_HelperBroker::getStaticHelper('QuickContact')->Eshop();
        //} catch (Exception $e) {
       // }
        // ****************************************************************************************
        
        
    }
}