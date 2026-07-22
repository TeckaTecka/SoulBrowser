<?php

class Admin_IndexController extends Zend_Controller_Action
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
    		'$("span a[title], span[title]").tooltip({
    			effect: "fade",
    			position: "top center",
    			opacity: 0.8,
    			predelay: 1000
    		 });'
    	);
    	
    	$ordersTab = new Admin_Model_DbTable_Orders();
    	$orders = $ordersTab->getNewOrders();
    	$this->view->orders = $orders;
    	//Zend_Debug::dump($orders);
    }
	public function ordersAction()
    {
    	
    }
	public function productsAction()
    {
    	
    }
	public function sitemapAction()
    {
    	
    }
	public function elementsAction()
    {
    	
    }
	public function marketingAction()
    {
    	
    }
	public function statisticsAction()
    {
    	
    }
	public function optionsAction()
    {
    	
    }
}