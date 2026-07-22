<?php

class Admin_Products_ProductsController extends Zend_Controller_Action
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
    		->prependStylesheet('/css/shared/table.css')
    		->prependStylesheet('/css/shared/paginator.css')
    		->prependStylesheet('/css/shared/form.css');
    	$this->view->jQuery()->addOnLoad(
    		'$("td span[title], td span a[title], th span[title]").tooltip({
    			effect: "fade",
    			position: "top center",
    			opacity: 0.8,
    			predelay: 1000
    		 });');
    	
    	$page = $this->_getParam('page');
    	// FORM SEARCH ****************************************************************************
    	$searchForm = new Admin_Form_Products_Products_Search();
    	$searchForm->setAction($this->view->url(array(), 'admin_products_products-index'));
    	$searchForm->setAttrib('id', 'form-search');
    	$this->view->searchForm = $searchForm;
    	// ****************************************************************************************
    	$productsTab = new Admin_Model_DbTable_Products();
    	$db = Zend_Registry::get('db');
    	
    	if (($this->getRequest()->isPost()) AND (($this->getRequest()->getPost('search_text')) <> ''))
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    		
	    	if ($searchForm->isValid($formData))
	    	{
	    		$products = $productsTab->searchProducts($formData['search_text']);
	    		$this->view->products = $products;
	    		//Zend_Debug::dump($products);
	    	}
    	}else{
    		$productsAll = $productsTab->getProducts($page);
    		$this->view->products = $productsAll;
    		//Zend_Debug::dump($productsAll);
    		
    		$adapter = new Zend_Paginator_Adapter_DbSelect(
	    		$db->select()
	    			->from('products')
	    			->where('flags IS NULL')
	    	);
	    	$paginator = new Zend_Paginator($adapter);
	    	$paginator->setItemCountPerPage(20)
	    		->setCurrentPageNumber($page);
	    	//Zend_Debug::dump($paginator);
	    	$this->view->paginator = $paginator;
    	}
    }
    public function addAction()
    {
    	$this->view->headLink()
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
    		 $("#description").cleditor({
    		 	width:"478",
    		 	height:"300"
    		 });');
        
    	// FORM PRODUCT ***************************************************************************
        $formProduct = new Admin_Form_Products_Products_Product();
    	$formProduct->setAction($this->view->url(array(), 'admin_products_products-add'));
    	$formProduct->setAttrib('id', 'form-add-product');
    	$this->view->formProduct = $formProduct;
    	
    	$manufactorersTab = new Admin_Model_DbTable_Manufacturers();
    	$manufactorers = $manufactorersTab->getPairs();
    	$formProduct->manufactorers->setMultiOptions($manufactorers);
    	$formProduct->manufactorers->setValue(1);
    	$vatTab = new Admin_Model_DbTable_Vat();
    	$vat = $vatTab->getVatsPairs();
    	$formProduct->vat->setMultiOptions($vat);
    	//Zend_Debug::dump($vat);
    	$availabilityTab = new Admin_Model_DbTable_Availability();
    	$availability = $availabilityTab->getAvailabilityAllPairs();
    	$formProduct->availability->setMultiOptions($availability);
    	
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    		
	    	if ($formProduct->isValid($formData))
	    	{
	    		$productsTab = new Admin_Model_DbTable_Products();
	    		if ($formData['code'] == ''){
	    			$data = array(
	    				'code'		=>	$productsTab->getFreeCode()
	    			);
			    	$formData['code'] = $data['code'];
			    	$formProduct->populate($data);
    			}
    			if ($formData['title_url'] == ''){
	    			$data = array(
	    				'title_url'		=>	$productsTab->Convert($formData['title'])
	    			);
			    	$formData['title_url'] = $data['title_url'];
			    	$formProduct->populate($data);
    			}
    			
    			if ($formData['title_menu'] == ''){
	    			$data = array(
	    				'title_menu'	=>	$formData['title']
	    			);
			    	$formData['title_menu'] = $data['title_menu'];
			    	$formProduct->populate($data);
    			}
    			$ok = true;
    			$codeExist = $productsTab->codeExist($formData['code']);
    			if ($codeExist){
	    			$formProduct->code
	    				->setDescription('Tento kód již existuje')
	    				->addDecorator('Description', array('class'	=>	'errors'));
	    			$ok = false;
	    		}
    			$urlExist = $productsTab->urlExist($formData['title_url']);
	    		if ($urlExist){
	    			$formProduct->title_url
	    				->setDescription('Tato URL adresa již existuje')
	    				->addDecorator('Description', array('class'	=>	'errors'));
	    			$ok = false;
	    		}
	    		if ($ok){
	    			$id = $productsTab->setProduct(
	    				$formData['code'],
	    				$formData['title'],
		    			$formData['title_menu'],
		    			$formData['title_url'],
		    			$formData['price'],
		    			$formData['manufactorers'],
		    			$formData['vat'],
		    			$formData['availability'],
		    			$formData['price_orig'],
		    			$formData['short_desc'],
		    			$formData['description'],
		    			0,
		    			0
		    		);
	    			
		    		$this->_helper->redirector->gotoRoute(array('id'	=>	$id), 'admin_products_products-edit');
	    		}
	    	}
        }
    }
	public function editAction()
    {
    	$this->view->headLink()
        	->prependStylesheet('/css/shared/form.css')
        	->prependStylesheet('/css/shared/table.css')
        	->prependStylesheet('/css/admin/product.css')
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
    		 $("#description").cleditor({
    		 	width:"478",
    		 	height:"300"
    		 });
    		 $("#product").click(function(){
    		 	$("#tab1").css({display: "block"});$("#product").addClass("active");
    			$("#tab2").css({display: "none"});$("#categories").removeClass("active");
    			$("#tab3").css({display: "none"});$("#pictures").removeClass("active");
    			$("#tab4").css({display: "none"});$("#parameters").removeClass("active");
    			$("#tab5").css({display: "none"});$("#settings").removeClass("active");
    		 });
    		 $("#categories").click(function(){
    			$("#tab1").css({display: "none"});$("#product").removeClass("active");
    			$("#tab2").css({display: "block"});$("#categories").addClass("active");
    			$("#tab3").css({display: "none"});$("#pictures").removeClass("active");
    			$("#tab4").css({display: "none"});$("#parameters").removeClass("active");
    			$("#tab5").css({display: "none"});$("#settings").removeClass("active");
    		 });
    		 $("#pictures").click(function(){
    			$("#tab1").css({display: "none"});$("#product").removeClass("active");
    			$("#tab2").css({display: "none"});$("#categories").removeClass("active");
    			$("#tab3").css({display: "block"});$("#pictures").addClass("active");
    			$("#tab4").css({display: "none"});$("#parameters").removeClass("active");
    			$("#tab5").css({display: "none"});$("#settings").removeClass("active");
    		 });
    		  $("#parameters").click(function(){
    			$("#tab1").css({display: "none"});$("#product").removeClass("active");
    			$("#tab2").css({display: "none"});$("#categories").removeClass("active");
    			$("#tab3").css({display: "none"});$("#pictures").removeClass("active");
    			$("#tab4").css({display: "block"});$("#parameters").addClass("active");
    			$("#tab5").css({display: "none"});$("#settings").removeClass("active");
    			$("#tab4-1").css({display: "block"});$("#parameter").addClass("active");
    			$("#tab4-2").css({display: "none"});$("#parameters-group").removeClass("active");
    		 });
    		 $("#parameter").click(function(){
    			$("#tab4-1").css({display: "block"});$("#parameter").addClass("active");
    			$("#tab4-2").css({display: "none"});$("#parameters-group").removeClass("active");
    		 });
    		 $("#parameters-group").click(function(){
    			$("#tab4-1").css({display: "none"});$("#parameter").removeClass("active");
    			$("#tab4-2").css({display: "block"});$("#parameters-group").addClass("active");
    		 });
    		 $("#settings").click(function(){
    			$("#tab1").css({display: "none"});$("#product").removeClass("active");
    			$("#tab2").css({display: "none"});$("#categories").removeClass("active");
    			$("#tab3").css({display: "none"});$("#pictures").removeClass("active");
    			$("#tab4").css({display: "none"});$("#parameters").removeClass("active");
    			$("#tab5").css({display: "block"});$("#settings").addClass("active");
    		 });
    		 
    		 ');
        
    	$id = $this->_getParam('id');
    	$this->view->product_id = $id;
    	$back = $this->_getParam('back');
    	
    	// FORM PRODUCT ***************************************************************************
        $formProduct = new Admin_Form_Products_Products_Product();
    	$formProduct->setAction($this->view->url(array('back'	=>	NULL), 'admin_products_products-edit'));
    	$formProduct->setAttrib('id', 'form-edit-product');
    	$this->view->formProduct = $formProduct;
    	$manufactorersTab = new Admin_Model_DbTable_Manufacturers();
    	$manufactorers = $manufactorersTab->getPairs();
    	$formProduct->manufactorers->setMultiOptions($manufactorers);
    	$vatTab = new Admin_Model_DbTable_Vat();
    	$vat = $vatTab->getVatsPairs();
    	$formProduct->vat->setMultiOptions($vat);
    	//Zend_Debug::dump($vat);
    	$availabilityTab = new Admin_Model_DbTable_Availability();
    	$availability = $availabilityTab->getAvailabilityAllPairs();
    	$formProduct->availability->setMultiOptions($availability);
    	
    	// FORM CATEGORIES ************************************************************************
    	$formCategories = new Admin_Form_Products_Products_Categories();
    	$formCategories->setAction($this->view->url(array('back'	=>	NULL), 'admin_products_products-edit'));
    	$formCategories->setAttrib('id', 'form-add-categories');
    	$this->view->formCategories = $formCategories;
    	$categoriesTab = new Admin_Model_DbTable_Categories();
    	//$categoriesTreeTab = new Admin_Model_DbTable_CategoriesTree();
    	$data = $categoriesTab->getCategoriesAll();
    	if ($data){
	    	for ($i = 0; $i < count($data); $i++) {
	    		//$countOfParents = $categoriesTreeTab->getCountOfParents($data[$i]['sub']);
	    		$options[$data[$i]['id']] = $data[$i]['title'];
	    	}
	    	$formCategories->categories->setMultiOptions($options);
    	}
    	// FORM PICS ******************************************************************************
    	$formPics = new Admin_Form_Products_Products_Pics();
    	$formPics->setAction($this->view->url(array('back'	=>	NULL), 'admin_products_products-edit'));
    	$formPics->setAttrib('enctype', 'multipart/form-data');
    	$formPics->setAttrib('id', 'form-add-pics');
    	$this->view->formPics = $formPics;
    	$picturesTab = new Admin_Model_DbTable_Pictures();
    	$pictures = $picturesTab->getPictures($id);
    	$this->view->pics = $pictures;
    	//Zend_Debug::dump($pictures);
    	$settingsTab = new Admin_Model_DbTable_Settings();
    	$productsSettings = $settingsTab->getFlag('products');
    	if ($productsSettings['watermark'] == 1){
    		$formPics->watermark->setValue(1);
    	}else{
    		$formPics->watermark->setAttrib('disabled', true);
    		$formPics->watermark->setDescription('Není povoleno. Změnit můžete v "Nastavení vodoznaku"');
    	}
    	
	    // FORM PARAMETERS **************************************************************************
    	$formParameters = new Admin_Form_Products_Products_Parameters();
    	$formParameters->setAction($this->view->url(array('back'	=>	2), 'admin_products_products-edit'));
    	$formParameters->setAttrib('id', 'form-add-parameters');
    	$this->view->formParameters = $formParameters;
    	$parametersTab = new Admin_Model_DbTable_Parameters();
    	$parametersPairs = $parametersTab->getParametersPairs();
    	$formParameters->parameters->setMultiOptions($parametersPairs);
    	$products2ParametersTab = new Admin_Model_DbTable_Products2Parameters();
    	$parameters = $products2ParametersTab->getParameters($id);
    	$this->view->parameters = $parameters;
    	//Zend_Debug::dump($parameters);
    	
    	// FORM PARAMETERS GROUP ******************************************************************
    	$formParametersGroup = new Admin_Form_Products_Products_ParametersGroup();
    	$formParametersGroup->setAction($this->view->url(array('back'	=>	42), 'admin_products_products-edit'));
    	$formParametersGroup->setAttrib('id', 'form-add-parameters-group');
    	$this->view->formParametersGroup = $formParametersGroup;
    	$parametersGroupsTab = new Admin_Model_DbTable_ParametersGroups();
    	$parametersGroupsPairs = $parametersGroupsTab->getGroupsPairs();
    	if ($parametersGroupsPairs){
    		$formParametersGroup->groups->setMultiOptions($parametersGroupsPairs);
    	}
    	    	
    	// FORM SETTINGS **************************************************************************
    	$formSettings = new Admin_Form_Products_Products_Settings();
    	$formSettings->setAction($this->view->url(array('back'	=>	NULL), 'admin_products_products-edit'));
    	$formSettings->setAttrib('id', 'form-add-settings');
    	$this->view->formSettings = $formSettings;
    	
    	$productsTab = new Admin_Model_DbTable_Products();
    	$products2CategoriesTab = new Admin_Model_DbTable_Products2Categories();
    	
    	$product = $productsTab->getProduct($id);
		$data = array(
			'code'			=>	$product['code'],
			'title'			=>	$product['title'],
			'title_menu'	=>	$product['title_menu'],
			'title_url'		=>	$product['title_url'],
			'price'			=>	$product['price'],
			'manufactorers'	=>	($product['manufacturers_id'])?$product['manufacturers_id']:1,
			'vat'			=>	$product['vat_id'],
			'availability'	=>	$product['availability_id'],
			'price_orig'	=>	$product['price_orig'],
			'short_desc'	=>	$product['short_desc'],
			'description'	=>	$product['description'],
			'show'			=>	$product['show'],
			'recommend'		=>	$product['recommend'],
			'news'			=>	$product['news']
		);
		$formProduct->populate($data);
    	
		$categories = $products2CategoriesTab->getProduct($id);
		//Zend_Debug::dump($categories);
		if ($categories){
			foreach ($categories as $item){
				$values[] =($item['categories_id']);
			}
			//Zend_Debug::dump($values);
			$formCategories->categories->setValue($values);
		}
		
		$formSettings->populate($data);
		
		if($back==1){
			$this->view->jQuery()->addOnLoad('$("#pictures").click();');
		}elseif ($back==2) {
			$this->view->jQuery()->addOnLoad('$("#parameters").click();');
		}elseif ($back==42) {
			$this->view->jQuery()->addOnLoad('$("#parameters").click();$("#parameters-group").click();');
		}
    	/*****************************************************************************************/
		
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    	
			// FORM PRODUCT ***********************************************************************
	    	$saveProduct = $this->getRequest()->getPost('saveProduct');
	    	if ($saveProduct)
	    	{
	    		if ($formProduct->isValid($formData))
	    		{
	    			if ($formData['code'] == ''){
		    			$data = array(
		    				'code'		=>	$productsTab->getFreeCode()
		    			);
				    	$formData['code'] = $data['code'];
				    	$formProduct->populate($data);
	    			}
	    			if ($formData['title_url'] == ''){
		    			$data = array(
		    				'title_url'		=>	$productsTab->Convert($formData['title'])
		    			);
				    	$formData['title_url'] = $data['title_url'];
				    	$formProduct->populate($data);
	    			}
	    			
	    			if ($formData['title_menu'] == ''){
		    			$data = array(
		    				'title_menu'	=>	$formData['title']
		    			);
				    	$formData['title_menu'] = $data['title_menu'];
				    	$formProduct->populate($data);
	    			}
	    			$ok = true;
	    			$codeExist = $productsTab->codeExist($formData['code']);
	    			if (($codeExist > 1) OR (($codeExist == 1) AND ($product['code'] != $formData['code']))){
		    			$formProduct->code
		    				->setDescription('Tento kód již existuje')
		    				->addDecorator('Description', array('class'	=>	'errors'));
		    			$ok = false;
		    		}
	    			$urlExist = $productsTab->urlExist($formData['title_url']);
		    		if (($urlExist > 1) OR (($urlExist == 1) AND ($product['title_url'] != $formData['title_url']))){
		    			$formProduct->title_url
		    				->setDescription('Tato URL adresa již existuje')
		    				->addDecorator('Description', array('class'	=>	'errors'));
		    			$ok = false;
		    		}
		    		if ($ok){
		    			$productsTab->updateProduct(
		    				$id,
		    				$formData['code'],
			    			$formData['title'],
				    		$formData['title_menu'],
				    		$formData['title_url'],
				    		$formData['price'],
				    		$formData['manufactorers'],
				    		$formData['vat'],
				    		$formData['availability'],
			    			$formData['price_orig'],
				    		$formData['short_desc'],
				    		$formData['description'],
				    		$product['show'],
				    		$product['recommend']
				    	);
		    		}			    	
	    		}
	    	}
			// FORM CATEGORIES ********************************************************************
	    	$saveCategories = $this->getRequest()->getPost('saveCategories');
	    	if ($saveCategories)
	    	{
	    		$products2CategoriesTab->delProduct($id);
	    		
	    		if (isset($formData['categories'])){
		    		foreach ($formData['categories'] as $item){
				    	$products2CategoriesTab->setProduct2Category($id, $item);
				    }
				}else{
					$formCategories->categories->setValue(array());
				}
				$formCategories->populate($formData);
	    	}
	    	
	    	// FORM PICS **************************************************************************
	    	$savePics = $this->getRequest()->getPost('savePics');
	    	if ($savePics)
	    	{
	    		$this->view->jQuery()->addOnLoad('$("#pictures").click();');
	    		
	    		if ($formPics->isValid($formData))
	    		{
		    		//Image
					$adapter = $formPics->pics->getTransferAdapter();
					//Zend_Debug::dump($adapter);
	    			$infoImage = $adapter->getFileInfo('pics');
	    			//Zend_Debug::dump($infoImage, $label='$infoImage: ', $echo=true);
	    			$picsCount = $picturesTab->getCount($id);
	    			$fileName = $data['code'].'_'.($picsCount + 1);
	    			$suffix = strtolower(substr($infoImage['pics']['name'],-4));
	    			//Zend_Debug::dump($fileName);
    				
	    			$adapter->addFilter('Rename',
					    						array('target'=>'data/jpg/products/'.$fileName.$suffix,
					    						'overwrite'=>true));
					
					$adapter->receive($infoImage['pics']['name']);
					
					$thumb = new Admin_Model_Thumb();
					//Thumb 100x100
	    			$thumb->createThumbAdaptive(
	    				'data/jpg/products/'.$fileName.$suffix,
	    				'data/jpg/products/100x100/'.$fileName.'.jpg',
	    				100,
	    				100
	    			);
	    			
					//Thumb 170x170
	    			$thumb->createThumbAdaptive(
	    				'data/jpg/products/'.$fileName.$suffix,
	    				'data/jpg/products/170x170/'.$fileName.'.jpg',
	    				170,
	    				170
	    			);
	    			
	    			//Thumb 800x500
	    			if ($formData['watermark']){
	    				$thumb->createThumbWatermark(
		    				'data/jpg/products/'.$fileName.$suffix,
		    				'data/jpg/products/800x500/'.$fileName.'.jpg',
		    				800,
		    				500,
		    				'data/png/watermark/watermark.png'
		    			);
	    			}else{
	    				$thumb->createThumbAdaptive(
		    				'data/jpg/products/'.$fileName.$suffix,
		    				'data/jpg/products/800x500/'.$fileName.'.jpg',
		    				800,
		    				500
		    			);
	    			}
	    			
	    			
	    			
	    			$picturesTab->delFile('data/jpg/products/'.$fileName.$suffix);
	    			$pic_id = $picturesTab->setPicture(
	    				$id,
	    				'data/jpg/products/100x100/'.$fileName.'.jpg',
	    				'data/jpg/products/170x170/'.$fileName.'.jpg',
	    				'data/jpg/products/800x500/'.$fileName.'.jpg'
	    			);
	    			
	    			$products2PicturesTab = new Admin_Model_DbTable_Products2Pictures();
	    			$products2PicturesTab->setPicture($id, $pic_id);
	    			
	    			$pictures = $picturesTab->getPictures($id);
    				$this->view->pics = $pictures;
    				
    				
	    		}
            
	    	}
	    	
    		// FORM PARAMETERS **********************************************************************
	    	$saveParameter = $this->getRequest()->getPost('saveParameter');
	    	if ($saveParameter)
	    	{
	    		$products2ParametersTab->setParameter(
    				$id,
    				$formData['parameters'],
		    		$formData['value']
		    	);
		    	
		    	$parameters = $products2ParametersTab->getParameters($id);
    			$this->view->parameters = $parameters;
	    	}
	    	
    		// FORM PARAMETERS GROUP **************************************************************
	    	$setGroup = $this->getRequest()->getPost('setGroup');
	    	if ($setGroup)
	    	{
	    		$this->_helper->redirector->gotoRoute(
	    			array(
	    				'id'		=>	$id,
	    				'tab'		=>	42,
	    				'group_id'	=>	$formData['groups']
	    			),
	    			'admin_products_products_parameter-add-parameters-group'
	    		);
	    	}
	    	
	    	// FORM SETTINGS **********************************************************************
	    	$saveSettings = $this->getRequest()->getPost('saveSettings');
	    	if ($saveSettings)
	    	{
	    		$productsTab->updateProduct(
    				$id,
    				$data['code'],
	    			$data['title'],
		    		$data['title_menu'],
		    		$data['title_url'],
		    		$data['price'],
		    		$data['manufactorers'],
		    		$data['vat'],
		    		$data['availability'],
	    			$data['price_orig'],
		    		$data['short_desc'],
		    		$data['description'],
		    		$formData['show'],
		    		$formData['recommend'],
		    		$formData['news']
		    	);
		    	$formSettings->populate($formData);
	    	}
    	}
    	
    	
	}
	public function addParametersGroupAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css')
    		->prependStylesheet('/css/shared/table.css');
    	$this->view->jQuery()->addOnLoad(
        	'$("form :input.tool-tip").tooltip({
        		effect: "fade",
        		offset: [20, 100],
        		opacity: 0.8
    		 });
    		 $("#tab4").css({display: "block"});
    		 ');
    	$id = $this->_getParam('id');
    	$this->view->product_id = $id;
    	$tab = $this->_getParam('tab');
    	$group_id = $this->_getParam('group_id');
    	
    	$products2ParametersTab = new Admin_Model_DbTable_Products2Parameters();
    	$parameters = $products2ParametersTab->getParameters($id);
    	$this->view->parameters = $parameters;
    	
    	$parametersGroupsTab = new Admin_Model_DbTable_ParametersGroups();
    	$parametersGroups = $parametersGroupsTab->getParameters($group_id);
    	//Zend_Debug::dump($parametersGroups);
    	
    	$formParametersGroup = new Zend_Form();
    	$formParametersGroup->setAction(
    		$this->view->url(
    			array(
    				'id'		=>	$id,
    				'tab'		=>	$tab,
    				'group_id'	=>	$group_id
    			),
    			'admin_products_products_parameter-add-parameters-group'
    		)
    	);
    	$formParametersGroup->setAttrib('id', 'form-add-parameters-group');
    	$elementDecorators = array(
			'ViewHelper',
			array('Label', array('separator'	=>	'')), 
			array('Description', array('tag'	=>	'span')),
			'Errors',
			array('HtmlTag', array('tag'	=>	'div', 'class'	=>	'element'))
		);
    	foreach ($parametersGroups as $param) {
    		$formParametersGroup->addElement('text', $param['id'], array(
			'decorators'	=>	$elementDecorators,
        	'label'			=>	$param['title']
		));
    	}
    	$buttonDecorators = array('ViewHelper');
    	$formParametersGroup->addElement('submit', 'setGroup', array(
			'decorators'	=>	$buttonDecorators,
        	'label'			=>	'Přidat'
		));
    	$formParametersGroup->addElement('submit', 'storno', array(
			'decorators'	=>	$buttonDecorators,
        	'label'			=>	'Storno'
		));
    	$this->view->formParametersGroup = $formParametersGroup;
    	
    	if ($this->getRequest()->isPost()) {
            $storno = $this->getRequest()->getPost('storno');
            if ($storno) {
            	$this->_helper->redirector->gotoRoute(
	            	array(
	            		'id'	=>	$id,
	            		'back'	=>	42
	            	),
	            	'admin_products_products-edit'
	            );
          	}
          	$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    	$setGroup = $this->getRequest()->getPost('setGroup');
            if ($setGroup) {
            	for ($i = 0; $i < count($parametersGroups); $i++) {
            		if ($formData[$i + 1]!=''){
	            		$products2ParametersTab->setParameter(
	            			$id,
	            			$parametersGroups[$i]['id'],
	            			$formData[$i + 1]
	            		);
            		}
            	}
            	$this->_helper->redirector->gotoRoute(
	            	array(
	            		'id'	=>	$id,
	            		'back'	=>	42
	            	),
	            	'admin_products_products-edit'
	            );
            }
    	}
    }
	public function delAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css');
    	
    	$id = $this->_getParam('id');
    	
    	$form = new Admin_Form_Products_Products_Del();
    	$form->setAction($this->view->url(array('id' => $id), 'admin_products_products-del'));
    	$this->view->form = $form;
    	
    	$productsTab = new Admin_Model_DbTable_Products();
    	
    	$product = $productsTab->getProduct($id);
    	$this->view->product = $product['title'];
    	
    	if ($this->getRequest()->isPost()) {
            $delete = $this->getRequest()->getPost('delete');
                        
            if ($delete != NULL) {
            	$productsTab->setFlag($id, 'delete');
          	}
            $this->_helper->redirector->gotoRoute(array(), 'admin_products_products-index');
    	}
    }
    public function picActiveAction()
    {
    	$product_id = $this->_getParam('product_id');
    	$id = $this->_getParam('id');
    	
    	$picturesTab = new Admin_Model_DbTable_Pictures();
    	$pictures = $picturesTab->changeActive(
    		$product_id,
    		$id
    	);
    	
    	$this->_helper->redirector->gotoRoute(
    		array(
    			'id'	=>	$product_id,
    			'back'	=>	1
    		),
    		'admin_products_products-edit'
    	);
    }
    public function delPictureAction()
    {
    	$product_id = $this->_getParam('product_id');
    	$id = $this->_getParam('id');
    	
    	$picturesTab = new Admin_Model_DbTable_Pictures();
    	$picturesTab->delPicture($id);
    	
    	$this->_helper->redirector->gotoRoute(
    		array(
    			'id'	=>	$product_id,
    			'back'	=>	1
    		),
    		'admin_products_products-edit'
    	);
    }
    public function moveRightAction()
    {
    	$product_id = $this->_getParam('product_id');
    	$id = $this->_getParam('id');
    	
    	$picturesTab = new Admin_Model_DbTable_Pictures();
    	$picturesTab->moveRight($product_id, $id);
    	
    	$this->_helper->redirector->gotoRoute(
    		array(
    			'id'	=>	$product_id,
    			'back'	=>	1
    		),
    		'admin_products_products-edit'
    	);
    }
	public function moveLeftAction()
    {
    	$product_id = $this->_getParam('product_id');
    	$id = $this->_getParam('id');
    	
    	$picturesTab = new Admin_Model_DbTable_Pictures();
    	$picturesTab->moveLeft($product_id, $id);
    	
    	$this->_helper->redirector->gotoRoute(
    		array(
    			'id'	=>	$product_id,
    			'back'	=>	1
    		),
    		'admin_products_products-edit'
    	);
    }
	public function delParameterAction()
    {
    	$product_id = $this->_getParam('id');
    	$back = $this->_getParam('back');
    	$param_id = $this->_getParam('param_id');
    	
    	$products2parametersTab = new Admin_Model_DbTable_Products2Parameters();
    	$products2parametersTab->delParameter($param_id);
    	
    	$this->_helper->redirector->gotoRoute(
    		array(
    			'id'	=>	$product_id,
    			'back'	=>	$back
    		),
    		'admin_products_products-edit'
    	);
    }
}