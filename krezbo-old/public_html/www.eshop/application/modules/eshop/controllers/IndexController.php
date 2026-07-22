<?php

class Eshop_IndexController extends Zend_Controller_Action
{
		
    public function init()
    {
    	$this->_helper->layout()->setLayout('eshop');
    	$this->_helper->eshop->initLayout();    	
    }

    public function indexAction()
    {
    	$this->view->headScript()
			->appendFile('/js/jquery/jquery.nivo.slider.pack.js')
			->setIndent("\t");
		
		$this->view->headLink()
			->prependStylesheet('/css/products.css')
			->appendStylesheet('/css/jquery/nivo-slider.css')
			->setIndent("\t");
		
       $this->view->jQuery()->addOnLoad(
    		'$("#slider").nivoSlider({
    			directionNavHide: false,
    			directionNav: false,
    			animSpeed: 1000,
    			pauseTime: 5000,
    			slices: 10,
    			boxCols: 6,
    			boxRows: 4
    		});'
		);
    	
    	$categoriesTab = new Eshop_Model_DbTable_Categories();
    	$categories = $categoriesTab->getCategories();
    	$data = '';
    	foreach ($categories as $category) {
    		$data .= $category['title'].',';
    	}
    	//Zend_Debug::dump($data);
    	$manufacrotersTab = new Eshop_Model_DbTable_Manufacturers();
    	$manufacroters = $manufacrotersTab->getManufacturers();
    	foreach ($manufacroters as $manufactory) {
    		$data .= $manufactory['title'].',';
    	}
    	
    	$this->setTitle();
    	$this->setMeta(
    		$data.'doporučujeme,novinky',
    		''
    	);
    	
        $pagesTab = new Eshop_Model_DbTable_Pages();
        $homePage = $pagesTab->getPage(1);
        $this->view->homePage = $homePage;
        //Zend_Debug::dump($homePage);
        
    	$count = 2;
    	
        $productTab = new Eshop_Model_DbTable_Products();
        $recommendProducts = $productTab->getRecommendProducts($count);
        $this->view->recommendProducts = $recommendProducts;
        //Zend_Debug::dump($recommendProducts);
        
        $recommendCount = $productTab->getRecommendCount();
        
        if ($recommendCount > $count){
        	$this->view->recommendMore = true;
        }
        // novinky
    	$newProducts = $productTab->getNewProducts($count);
        $this->view->newProducts = $newProducts;
        //Zend_Debug::dump($newProducts);
        
        $newCount = $productTab->getNewCount();
        
        if ($newCount > $count){
        	$this->view->newMore = true;
        }
        
        
    }
    public function categoryAction()
    {
    	$this->view->headLink()
    		->appendStylesheet('/css/products.css')
    		->appendStylesheet('/css/shared/paginator.css');
    	
    	$category_url = $this->_getParam('category');
    	$page = $this->_getParam('page');
    	$this->view->page = $page;
    	
    	$categoriesTab = new Eshop_Model_DbTable_Categories();
    	$category = $categoriesTab->getCategoryByTitleUrl($category_url);
    	$this->view->category = $category;
    	//Zend_Debug::dump($category);
    	
    	$productsTab = new Eshop_Model_DbTable_Products();
    	$products = $productsTab->getProductsByCategory($category_url, $page);
    	//Zend_Debug::dump($products);
    	$this->view->products = $products;
    	
    	$db = Zend_Registry::get('db');
    	$adapter = new Zend_Paginator_Adapter_DbSelect(
    		$db->select()
    			->from('products')
    			->join('products2categories',		  
					'products2categories.products_id=products.id', array())
				->join('categories',		  
					'products2categories.categories_id=categories.id', array())
				->where('categories.title_url = ?', $category_url)
				->where('products.show = ?', 1)
    	);
    	$paginator = new Zend_Paginator($adapter);
    	$paginator->setItemCountPerPage(8)
    		->setCurrentPageNumber($page);
    	//Zend_Debug::dump($paginator);
    	$this->view->paginator = $paginator;
    	
    	$data = '';
    	if ($products) {
	    	foreach ($products as $product) {
	    		$data .= $product['title'].',';
	    	}
    	}
    	$this->setTitle($category['title']);
    	$this->setMeta(
    		$data.$category['title'],
    		'Kategorie: '.$category['title'].': '.$category['description']
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
    	
    	$category_url = $this->_getParam('category');
    	$categoriesTab = new Eshop_Model_DbTable_Categories();
    	$category = $categoriesTab->getCategoryByTitleUrl($category_url);
    	$this->view->category = $category;
    	//Zend_Debug::dump($category);
    	
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
    		$product['title'].','.$category['title'],
    		'Produkt: '.$product['title'].': '.$product['short_desc']
    	);
    }
    public function contactsAction()
    {
    	$settingsTab = new Eshop_Model_DbTable_Settings();
    	$eshop = $settingsTab->getFlag('eshop');
    	$this->view->eshop = $eshop;
    	//Zend_Debug::dump($eshop);
    	$bank = $settingsTab->getFlag('bank');
    	$this->view->bank = $bank;
    	//Zend_Debug::dump($bank);
    	
    	$this->setTitle('Kontakty');
    	$this->setMeta(
    		'kontakty,'.$eshop['title'].','.$eshop['email'].','.$eshop['street'].','.$eshop['city'].','.$eshop['ic'].','.$eshop['dic'].','.$eshop['phone'].','.$eshop['mobile'].','.$eshop['fax'],
    		'Kontakt: '.$eshop['email'].': '.$eshop['phone'].','.$eshop['mobile'].','.$eshop['fax']
    	);
    }
    public function sitemapAction()
    {
    	$this->setTitle('Mapa stránek');
    	$this->setMeta(
    		'Mapa stránek',
    		'Mapa stránek'
    	);
    }
	public function recommendAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/products.css')
    		->appendStylesheet('/css/shared/paginator.css');
    	
    	$page = $this->_getParam('page');
    	$this->view->page = $page;
    	
        $productTab = new Eshop_Model_DbTable_Products();
        $recommendProducts = $productTab->getRecommendProducts(9, $page);
        $this->view->recommendProducts = $recommendProducts;
        //Zend_Debug::dump($recommendProducts);
        
        $db = Zend_Registry::get('db');
    	$adapter = new Zend_Paginator_Adapter_DbSelect(
	    	$db->select()
	    		->from('products')
	    		->where('products.recommend = ?', 1)
				->where('products.show = ?', 1)
				//->where('products.flags != ?', '"delete"')
				->where('products.flags IS NULL')
		);
    	$paginator = new Zend_Paginator($adapter);
    	$paginator->setItemCountPerPage(9)
    		->setCurrentPageNumber($page);
    	//Zend_Debug::dump($paginator);
    	$this->view->paginator = $paginator;
    	
    	$data = '';
    	foreach ($recommendProducts as $product) {
    		$data .= $product['title'].',';
    	}
    	$this->setTitle('Doporučujeme');
    	$this->setMeta(
    		'Doporučujeme,'.$data,
    		'Toto zboží bychom si sami koupili, doporučujeme'
    	);
    }
    public function recommendProductAction()
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
    		$product['title'].',doporučujeme',
    		'Produkt: '.$product['title'].': '.$product['short_desc']
    	);
    }
	public function newsAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/products.css')
    		->appendStylesheet('/css/shared/paginator.css');
    	
    	$page = $this->_getParam('page');
    	$this->view->page = $page;
    	
        $productTab = new Eshop_Model_DbTable_Products();
        $newProducts = $productTab->getNewProducts(9, $page);
        $this->view->newProducts = $newProducts;
        //Zend_Debug::dump($newProducts);
        
        $db = Zend_Registry::get('db');
    	$adapter = new Zend_Paginator_Adapter_DbSelect(
	    	$db->select()
	    		->from('products')
	    		->where('products.news = ?', 1)
				->where('products.show = ?', 1)
				//->where('products.flags != ?', 'delete')
				->where('products.flags IS NULL')
		);
    	$paginator = new Zend_Paginator($adapter);
    	$paginator->setItemCountPerPage(9)
    		->setCurrentPageNumber($page);
    	//Zend_Debug::dump($paginator);
    	$this->view->paginator = $paginator;
    	
    	$data = '';
    	foreach ($newProducts as $product) {
    		$data .= $product['title'].',';
    	}
    	$this->setTitle('Novinky');
    	$this->setMeta(
    		'Doporučujeme,'.$data,
    		'Nově přidané zboží'
    	);
    }
	public function newsProductAction()
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
    		$product['title'].',novinka',
    		'Produkt: '.$product['title'].': '.$product['short_desc']
    	);
    }
    public function searchAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/products.css')
    		->appendStylesheet('/css/shared/paginator.css');
    		
    	$search = $this->_getParam('text_search');// z formu
    	
    	$this->view->search = $search;
    	$page = $this->_getParam('page');
    	$this->view->page = $page;
    	
    	$formSearch = new Eshop_Form_Search();
    	$formSearch->setAction($this->view->url(array(), 'eshop_index_search'));
    	$formSearch->setAttrib('id', 'form-search');
    	$formSearch->text_search->setValue($search);
    	$this->view->formSearch = $formSearch;
    	
    	$productTab = new Eshop_Model_DbTable_Products();
    	$products = $productTab->searchProducts($search);
    	$this->view->products = $products;
    	//Zend_Debug::dump($products);
    	
    	$data = '';
    	foreach ($products as $product) {
    		$data .= $product['title'].',';
    	}
    	$this->setTitle('Vyhledávání');
    	$this->setMeta(
    		'vyhledat,'.$data,
    		'Vyhledané zboží na dotaz "'.$search.'"'
    	);
    }
    public function pagesAction()
    {
    	$page_url = $this->_getParam('page');
    	
    	$pagesTab = new Eshop_Model_DbTable_Pages();
    	$page = $pagesTab->getPageByTitleUrl($page_url);
        $this->view->page = $page;       
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