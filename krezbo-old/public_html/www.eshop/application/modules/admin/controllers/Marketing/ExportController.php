<?php

class Admin_Marketing_ExportController extends Zend_Controller_Action
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
    		->appendStylesheet('/css/shared/buttons.css');
    }
    
    public function zboziczAction()
    {
    	$productsTab = new Admin_Model_DbTable_Products();
    	$products = $productsTab->getProductsForEsport();
    	$this->view->products = $products;
    	//Zend_Debug::dump($products);
    	
    	foreach ($products as $product) {
    		$items[] = array(
	    		'product'		=>	$product['title'],
	    		'description'	=>	$product['short_desc'],
	    		'url'			=>	$product['url'],
	    		'imgUrl'		=>	$product['imgUrl'],
	    		'price'			=>	$product['price'],
	    		'vat'			=>	$product['vat'],
	    		'priceVat'		=>	$product['priceVat'],
	    		'dues'			=>	0,
	    		'deliveryDate'	=>	5,   // 0 skladem, 1-7 do týdne, 8 více jak týden, -1 neznámá
	    		'itemType'		=>	'new', // bazaar
	    		'manufacturer'	=>	$product['manufacturer'],
	    		'categoryText'	=>	$product['category_title']
	    	);
    	}
    	$zbozicz = new Admin_Model_Zbozicz();
    	$zbozicz->build($items);
    }
    public function sitemapAction()
    {
    	$productsTab = new Admin_Model_DbTable_Products();
    	$products = $productsTab->getProductsForEsport();
    	$this->view->products = $products;
    	//Zend_Debug::dump($products);
    	
    	foreach ($products as $product) {
    		$items[] = array(
	    		'loc'			=>	$product['url'],
	    		'lastmod'		=>	date('Y-m-d'),
	    		'changefreq'	=>	'monthly', // always, hourly, daily, weekly, monthly, yearly, never
    			'priority'		=>	0.5 // 1 - 0.0
	    	);
    	}
    	$sitemap = new Admin_Model_Sitemap();
    	$sitemap->build($items);
    }
}