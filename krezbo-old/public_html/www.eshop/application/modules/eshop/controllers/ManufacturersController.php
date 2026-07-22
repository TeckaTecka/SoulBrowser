<?php

class Eshop_ManufacturersController extends Zend_Controller_Action
{
		
    public function init()
    {
    	$this->_helper->layout()->setLayout('eshop');
    	$this->_helper->eshop->initLayout();    	
    }

    public function indexAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/products.css')
    		->prependStylesheet('/css/shared/paginator.css');
    		
    	$page = $this->_getParam('page');
    	$this->view->page = $page;
    	
        $manufacturersTab = new Eshop_Model_DbTable_Manufacturers();
        $manufacturers = $manufacturersTab->getManufacturers($page);
        $this->view->manufacturers = $manufacturers;
        //Zend_Debug::dump($manufacturers);
        
        $db = Zend_Registry::get('db');
    	$adapter = new Zend_Paginator_Adapter_DbSelect(
    		$db->select()
    			->from('manufacturers')
    			->where('id <> ?', 1)
    	);
    	$paginator = new Zend_Paginator($adapter);
    	$paginator->setItemCountPerPage(8)
    		->setCurrentPageNumber($page);
    	//Zend_Debug::dump($paginator);
    	$this->view->paginator = $paginator;
    	
    	$data = '';
    	foreach ($manufacturers as $item) {
    		$data .= $item['title'].',';
    	}
    	$this->setTitle('Výrobci');
    	$this->setMeta(
    		$data.'výrobci',
    		'Výrobci: '.$data
    	);
    }
    public function manufacturyAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/products.css')
    		->prependStylesheet('/css/shared/paginator.css');
    	
    	$manufactury_url = $this->_getParam('manufactury');
    	$page = $this->_getParam('page');
    	$this->view->page = $page;
    	
    	$manufacturersTab = new Eshop_Model_DbTable_Manufacturers();
    	$manufactury = $manufacturersTab->getManufacturyByTitleUrl($manufactury_url);
    	$this->view->manufactury = $manufactury;
    	//Zend_Debug::dump($manufactory);
    	
    	$productsTab = new Eshop_Model_DbTable_Products();
    	$products = $productsTab->getProductsByManufactury($manufactury_url, $page);
    	//Zend_Debug::dump($products);
    	$this->view->products = $products;
    	
    	$db = Zend_Registry::get('db');
    	$adapter = new Zend_Paginator_Adapter_DbSelect(
    		$db->select()
    			->from('products')
    			->join('manufacturers',		  
		       		  'manufacturers.id = products.manufacturers_id', array())
				->where('manufacturers.title_url = ?', $manufactury_url)
				->where('products.show = ?', 1)
    	);
    	$paginator = new Zend_Paginator($adapter);
    	$paginator->setItemCountPerPage(8)
    		->setCurrentPageNumber($page);
    	//Zend_Debug::dump($paginator);
    	$this->view->paginator = $paginator;
    	
    	$data = '';
    	foreach ($products as $product) {
    		$data .= $product['title'].',';
    	}
    	$this->setTitle($manufactury['title']);
    	$this->setMeta(
    		$data.$manufactury['title'],
    		'Výrobce: '.$manufactury['title'].': '.$manufactury['description']
    	);
    }
    public function productAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/products.css')
    		->prependStylesheet('/css/table.css');
    	
    	$this->view->headLink()->prependStylesheet('/css/prettyphoto/prettyphoto.css');
    	$this->view->headScript()->appendFile('/js/prettyphoto/jquery.prettyphoto.js');    	
    	$this->view->jQuery()->addOnLoad(
    		'$(".gallery a[rel^='."'prettyPhoto'".']").prettyPhoto({
    			animationSpeed:'."'slow'".',
    			theme:'."'light_rounded'".',
    			slideshow:5000
    		 });');
    	
    	$page = $this->_getParam('page');
    	$this->view->page = $page;
    	
    	$manufactury_url = $this->_getParam('manufactury');
    	$manufacturersTab = new Eshop_Model_DbTable_Manufacturers();
    	$manufactury = $manufacturersTab->getManufacturyByTitleUrl($manufactury_url);
    	$this->view->manufactury = $manufactury;
    	//Zend_Debug::dump($manufactory);
    	
    	$title_url = $this->_getParam('product');
    	$productsTab = new Eshop_Model_DbTable_Products();
    	$product = $productsTab->getProductByTitleUrl($title_url);
    	//Zend_Debug::dump($product);
    	$this->view->product = $product;
    	
    	$products2parametersTab = new Admin_Model_DbTable_Products2Parameters();
    	$parameters = $products2parametersTab->getParameters($product['id']);
    	$this->view->parameters = $parameters;
    	//Zend_Debug::dump($parameters);
    	
    	$this->setTitle($product['title']);
    	$this->setMeta(
    		$product['title'].','.$manufactury['title'],
    		'Produkt: '.$product['title'].': '.$product['short_desc']
    	);
    }
    
    
	private function setTitle($title = '')
    {
    	$this->view->title = $title;
		$this->view->headTitle(
			$this->view->title,
			'PREPEND'
		);
    }
    private function setMeta($keywords = '', $description = '')
    {
    	// Nastaveni META 'keywords' a 'description'
        $this->view->headMeta()->appendName(
        	'keywords',
        	'Krezbo,eshop,čerpadla'.(($keywords)?','.$keywords:'')	       	
        );
    	$this->view->headMeta()->appendName(
    		'description',
    		(($description)?$description.'. ':'').'Nabízíme prodej: čerpadel, domácích vodáren, ručních pump, zahradních hadic, náhradních dílů k čerpadlum i vodárnám v rámci maloobchodu i velkoobchodu.'
    	);
    }
}