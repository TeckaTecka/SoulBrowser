<?php

class Cart_IndexController extends Zend_Controller_Action
{
	private $cart;
	private $user;
		
    public function init()
    {
    	/*if (Zend_Session::namespaceIsset('Cart')){
        	$this->cart = Zend_Session::namespaceGet('Cart');
    		if ($this->cart['products'] == NULL){
    			$this->cart = array();
    		}
    	}else{
    	*/	$this->cart = new Zend_Session_Namespace('Cart',true);
    		if ($this->cart->products == NULL){
    			$this->cart->unsetAll();
    		}
        //}
        
    	$auth = Zend_Auth::getInstance();
    	if($auth->hasIdentity()){
    		$this->user = $auth->getIdentity();
    	}
    	   	
    }
    public function indexAction()
    {
    	//$this->createPDF(1);
    	$this->_helper->layout()->setLayout('eshop');
    	$this->_helper->eshop->initLayout();
    	
    	$this->view->headLink()->prependStylesheet('/css/table.css');
    	
    	if (isset($this->cart->products)){
    		$products = $this->cart->products;
	    	//Zend_Debug::dump($products);
	    	$this->view->products = $products;
    	}
    }
	public function addAction()
    {
    	//$this->_helper->layout()->setLayout('eshop');
    	//$this->_helper->eshop->initLayout();
    	$page = $this->_getParam('page');
    	$idCategory = $this->_getParam('idCategory');// id kategorie
    	$idProduct = $this->_getParam('idProduct');// id zbozi
    	$back = $this->_getParam('back');// zpet
    	
    	$productsTab = new Cart_Model_DbTable_Products();
    	$product = $productsTab->getProduct($idProduct);
    	//Zend_Debug::dump($product);
    	    	
    	$categoriesTab = new Cart_Model_DbTable_Categories();
    	$category = $categoriesTab->getCategory($idCategory);
    	//Zend_Debug::dump($category);
    	
    	$tmp = $this->cart->products;
    	if (isset($tmp[$idProduct]['count'])){
    		$tmp[$idProduct]['count'] += 1;
    	}else{
    		$tmp[$idProduct] = $product;
    		$tmp[$idProduct]['count'] = 1;
    	}
    	$this->cart->products = $tmp;
    	//}
    	if ($back==1){
    		$this->_helper->redirector->gotoRoute(
    			array(
    				'category'	=>	$category['title_url'],
    				'page'		=>	$page
    			),
    			'eshop_index_category'
    		);
    	}elseif ($back==2){
    		$this->_helper->redirector->gotoRoute(
    			array(),
    			'cart_index_index'
    		);
    	}elseif ($back==3){
    		$this->_helper->redirector->gotoRoute(
    			array(),
    			'eshop_index_index'
    		);
    	}elseif ($back==4){
    		$this->_helper->redirector->gotoRoute(
    			array(
    				'category'	=>	$category['title_url'],
    				'product'	=>	$product['title_url'],
    				'page'		=>	$page
    			),
    			'eshop_index_product'
    		);
    	}
    	
    	//Zend_Debug::dump($this->cart->products,'Cart');
    }
	public function addManufacturyAction()
    {
    	//$this->_helper->layout()->setLayout('eshop');
    	//$this->_helper->eshop->initLayout();
    	$page = $this->_getParam('page');
    	$idManufactury = $this->_getParam('idManufactury');// id vyrobce
    	$idProduct = $this->_getParam('idProduct');// id zbozi
    	$back = $this->_getParam('back');// zpet
    	
    	$productsTab = new Cart_Model_DbTable_Products();
    	$product = $productsTab->getProduct($idProduct);
    	//Zend_Debug::dump($product);
    	    	
    	$manufacturersTab = new Eshop_Model_DbTable_Manufacturers();
    	$manufactury = $manufacturersTab->getManufactury($idManufactury);
    	//Zend_Debug::dump($manufactury);
    	
    	$tmp = $this->cart->products;
    	if (isset($tmp[$idProduct]['count'])){
    		$tmp[$idProduct]['count'] += 1;
    	}else{
    		$tmp[$idProduct] = $product;
    		$tmp[$idProduct]['count'] = 1;
    	}
    	$this->cart->products = $tmp;
    	//}
    	if ($back==1){
    		$this->_helper->redirector->gotoRoute(
    			array(
    				'page'		=>	$page,
    				'manufactury'	=>	$manufactury['title_url']
    			),
    			'eshop_manufacturers_manufactury'
    		);
    	}elseif ($back==2){
    		$this->_helper->redirector->gotoRoute(
    			array(
    				'manufactury'	=>	$manufactury['title_url'],
    				'product'		=>	$product['title_url']
    			),
    			'eshop_manufacturers_product'
    		);
    	}
    	//Zend_Debug::dump($this->cart->products,'Cart');
    }
    public function addRecommendAction()
    {
    	//$this->_helper->layout()->setLayout('eshop');
    	//$this->_helper->eshop->initLayout();
    	
    	$page = $this->_getParam('page');
    	$idProduct = $this->_getParam('idProduct');
    	$back = $this->_getParam('back');
    	
    	$productsTab = new Cart_Model_DbTable_Products();
    	$product = $productsTab->getProduct($idProduct);
    	//Zend_Debug::dump($product);
    	
    	$tmp = $this->cart->products;
    	if (isset($tmp[$idProduct]['count'])){
    		$tmp[$idProduct]['count'] += 1;
    	}else{
    		$tmp[$idProduct] = $product;
    		$tmp[$idProduct]['count'] = 1;
    	}
    	$this->cart->products = $tmp;
    	
    	if ($back==1){
    		$this->_helper->redirector->gotoRoute(
    			array(
    				'page'		=>	$page
    			),
    			'eshop_index_recommend'
    		);
    	}elseif ($back==2){
    		$this->_helper->redirector->gotoRoute(
    			array(
    				'page'		=>	$page,
    				'product'	=>	$product['title_url']
    			),
    			'eshop_index_recommend-product'
    		);
    	}
    }
	public function addNewsAction()
    {
    	//$this->_helper->layout()->setLayout('eshop');
    	//$this->_helper->eshop->initLayout();
    	
    	$page = $this->_getParam('page');
    	$idProduct = $this->_getParam('idProduct');
    	$back = $this->_getParam('back');
    	
    	$productsTab = new Cart_Model_DbTable_Products();
    	$product = $productsTab->getProduct($idProduct);
    	//Zend_Debug::dump($product);
    	
    	$tmp = $this->cart->products;
    	if (isset($tmp[$idProduct]['count'])){
    		$tmp[$idProduct]['count'] += 1;
    	}else{
    		$tmp[$idProduct] = $product;
    		$tmp[$idProduct]['count'] = 1;
    	}
    	$this->cart->products = $tmp;
    	
    	if ($back==1){
    		$this->_helper->redirector->gotoRoute(
    			array(
    				'page'		=>	$page
    			),
    			'eshop_index_news'
    		);
    	}elseif ($back==2){
    		$this->_helper->redirector->gotoRoute(
    			array(
    				'page'		=>	$page,
    				'product'	=>	$product['title_url']
    			),
    			'eshop_index_news-product'
    		);
    	}
    }
    public function removeAction()
    {
    	$idProduct = $this->_getParam('idProduct');// id zbozi
    	$back = $this->_getParam('back');// zpet
    	
    	if (Zend_Session::namespaceIsset('Cart')){
    		$tmp = $this->cart->products;
    		//$tmpReverse = array_reverse($tmp, true);
    		//$key = array_search($idProduct, $tmpReverse);
    		//unset($tmpReverse[$key]);
    		//$tmp = array_reverse($tmpReverse, false);
    		$tmp[$idProduct]['count']--;
    		$this->cart->products = $tmp;
    	}
    	if ($back==1){
    		//$this->_helper->redirector->gotoRoute(array('category'	=>	$category['title_url']),
    		//									  'default_index_category');
    	}elseif ($back==2){
    		$this->_helper->redirector->gotoRoute(array(),
    											  'cart_index_index');
    	}
    }
    public function delAction()
    {
    	$idProduct = $this->_getParam('idProduct');// id zbozi
    	$back = $this->_getParam('back');// zpet
    	
    	if (Zend_Session::namespaceIsset('Cart')){
    		$tmp = $this->cart->products;
    		//$key = array_search($idProduct, $tmp);
    		//while (($key<>FALSE) or ($key===0)){
    			unset($tmp[$idProduct]);
    		//	$key = array_search($idProduct, $tmp);
    		//}
    		$this->cart->products = $tmp;
    	}
    	if ($back==1){
    		//$this->_helper->redirector->gotoRoute(array('category'	=>	$category['title_url']),
    		//									  'default_index_category');
    	}elseif ($back==2){
    		$this->_helper->redirector->gotoRoute(array(),
    											  'cart_index_index');
    	}
    }
    public function emptyAction()
    {
    	$back = $this->_getParam('back');// zpet
    	
    	$this->cart->unsetAll();
    	
    	if ($back==1){
    		//$this->_helper->redirector->gotoRoute(array('category'	=>	$category['title_url']),
    		//									  'default_index_category');
    	}elseif ($back==2){
    		$this->_helper->redirector->gotoRoute(array(),
    											  'cart_index_index');
    	}
    }
    public function addressesAction()
    {
    	$this->view->headLink()->prependStylesheet('/css/form.css');
    	
    	$this->view->jQuery()->addOnLoad(
    		'$("#fieldset-billingInformation div.radions #addressType-1").click(function(){
    			$("#fieldset-billingInformation div.person").css({display: "block"});
    			$("#fieldset-billingInformation div.company").css({display: "none"});
    		 });
    		 $("#fieldset-billingInformation div.radions #addressType-2").click(function(){
    		 	$("#fieldset-billingInformation div.person").css({display: "none"});
    			$("#fieldset-billingInformation div.company").css({display: "block"});
    		 });
    		 $("#fieldset-deliveryInformation div.radions #addressType2-1").click(function(){
    			$("#fieldset-deliveryInformation div.person").css({display: "block"});
    			$("#fieldset-deliveryInformation div.company").css({display: "none"});
    		 });
    		 $("#fieldset-deliveryInformation div.radions #addressType2-2").click(function(){
    		 	$("#fieldset-deliveryInformation div.person").css({display: "none"});
    			$("#fieldset-deliveryInformation div.company").css({display: "block"});
    		 });
    		 $("#deliveryAddress").click(function(){
    		 	$("#fieldset-deliveryInformation").toggle();
    		 });
    	
    		 $("#tabs li span").click(function(){
    			$("#cart form").toggle();
    			$("#tabs li").toggle();
    		 });'
    		 /*$("nav#options span.services").click(function(){
    			$("nav#options #services").toggle("slow");
    		 });
    		 $("nav#options span.login-registration").click(
    		 	function(){
    		 		var el = $("div#panel");
					if (el.css("display") == "none"){
						$("div#panel").slideDown("slow");
					}else{
						$("div#panel").slideUp("slow");
					}
    		 		$("#toggle span").toggle();
    		 	}
    		 );*/
		);
		
    	$this->_helper->layout()->setLayout('eshop');
    	$this->_helper->eshop->initLayout();
    	
    	$form = new Cart_Form_Addresses();
    	$form->setAction($this->view->url(array(), 'cart_index_addresses'));
    	$form->setAttrib('id', 'form-Addresses');
    	$this->view->form = $form;
    	
    	$countriesTab = new Cart_Model_DbTable_Countries();
    	$countries = $countriesTab->getCountries();
    	for ($i = 0; $i < count($countries); $i++) {
    		$options[$countries[$i]['id']] = $countries[$i]['country'];
    	}
    	$form->country->setMultiOptions($options);
    	$form->country2->setMultiOptions($options);
    	
    	if ($this->user){
    		$UAform = new Cart_Form_UsersAddresses();
	    	$UAform->setAction($this->view->url(array(), 'cart_index_addresses'));
	    	$UAform->setAttrib('id', 'form-Users-Addresses');
	    	$this->view->UAform = $UAform;
	    	
	    	$addressesTab = new Cart_Model_DbTable_Addresses();
    		$addresses = $addressesTab->getAddressesByUserID($this->user['id']);
	    	//Zend_Debug::dump($addresses);
	    	$options = array();
	    	$options[''] = '-Vyberte ze seznamu-';
	    	for ($i = 0; $i < count($addresses); $i++) {
	    		$options[$addresses[$i]['id']] = $addresses[$i]['street'].' '.$addresses[$i]['street_nr'].', '.$addresses[$i]['city'].', '.$addresses[$i]['zip'];
	    	}
	    	$UAform->usersBillingAddress->setMultiOptions($options);
	    	$UAform->usersDeliveryAddress->setMultiOptions($options);
    	}
    	if ($this->getRequest()->isPost() AND ($this->getRequest()->getPost('usersAddressesSubmit')))
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData, '$formData');
	    	$UAform->usersBillingAddress->setValue($formData['usersBillingAddress']);
	    	$UAform->usersDeliveryAddress->setValue($formData['usersDeliveryAddress']);
	    	foreach ($addresses as $address) {
	    		$data[$address['id']] = $address;
	    	}
	    	$addresses = array();
	    	//Zend_Debug::dump($data, '$data');
	    	if ($formData['usersBillingAddress']){
	    		$billingAddress_id = $formData['usersBillingAddress'];
	    		$addresses['addressType'] = ($data[$billingAddress_id]['company_name'])?2:1;
	    		$billingAddress = $data[$billingAddress_id];
	    		//Zend_Debug::dump($billingAddress, '$billingAddress');
	    		$addresses['personTitle'] = $billingAddress['person_title'];
	    		$addresses['personName'] = $billingAddress['person_name'];
	    		$addresses['personSurname'] = $billingAddress['person_surname'];
	    		$addresses['companyName'] = $billingAddress['company_name'];
	    		$addresses['companyIC'] = $billingAddress['company_identification'];
	    		$addresses['companyDIC'] = $billingAddress['company_vat'];
	    		$addresses['contactPerson'] = $billingAddress['contact_person'];
	    		$addresses['email'] = $billingAddress['contact_email'];
	    		$addresses['phone'] = $billingAddress['contact_phone'];
	    		$addresses['street'] = $billingAddress['street'];
	    		$addresses['street_nr'] = $billingAddress['street_nr'];
	    		$addresses['city'] = $billingAddress['city'];
	    		$addresses['zip'] = $billingAddress['zip'];
	    		$addresses['country'] = $billingAddress['countries_id'];
	    		
	    		$addresses['deliveryAddress'] = ($formData['usersDeliveryAddress'])?0:1;
	    		
	    		$deliveryAddress_id = $formData['usersDeliveryAddress'];
	    		if ($deliveryAddress_id){
	    			$this->view->jQuery()->addOnLoad('$("#deliveryAddress").click();');
	    			$addresses['addressType2'] = ($data[$deliveryAddress_id]['company_name'])?2:1;
		    		$deliveryAddress = $data[$deliveryAddress_id];
		    		$addresses['personTitle2'] = $deliveryAddress['person_title'];
		    		$addresses['personName2'] = $deliveryAddress['person_name'];
		    		$addresses['personSurname2'] = $deliveryAddress['person_surname'];
		    		$addresses['companyName2'] = $deliveryAddress['company_name'];
		    		$addresses['companyIC2'] = $deliveryAddress['company_identification'];
		    		$addresses['companyDIC2'] = $deliveryAddress['company_vat'];
		    		$addresses['contactPerson2'] = $deliveryAddress['contact_person'];
		    		$addresses['email2'] = $deliveryAddress['contact_email'];
		    		$addresses['phone2'] = $deliveryAddress['contact_phone'];
		    		$addresses['street2'] = $deliveryAddress['street'];
		    		$addresses['street_nr2'] = $deliveryAddress['street_nr'];
		    		$addresses['city2'] = $deliveryAddress['city'];
		    		$addresses['zip2'] = $deliveryAddress['zip'];
		    		$addresses['country2'] = $deliveryAddress['countries_id'];
	    		}else{
	    			$addresses['addressType2'] = 1;
		    		$addresses['personTitle2'] = NULL;
		    		$addresses['personName2'] = NULL;
		    		$addresses['personSurname2'] = NULL;
		    		$addresses['companyName2'] = NULL;
		    		$addresses['companyIC2'] = NULL;
		    		$addresses['companyDIC2'] = NULL;
		    		$addresses['contactPerson2'] = NULL;
		    		$addresses['email2'] = NULL;
		    		$addresses['phone2'] = NULL;
		    		$addresses['street2'] = NULL;
		    		$addresses['street_nr2'] = NULL;
		    		$addresses['city2'] = NULL;
		    		$addresses['zip2'] = NULL;
		    		$addresses['country2'] = NULL;
	    		}
	    		//Zend_Debug::dump($addresses);
	    		$this->cart->addresses = $addresses;
	    		$form->populate($addresses);
	    	}else{
	    		$this->cart->addresses = NULL;
	    	}
	    	
    	}
    	
    	
    	if ($this->getRequest()->isPost())
    	{
    		$addressSubmit = $this->getRequest()->getPost('addressSubmit');
    		$prev = $this->getRequest()->getPost('prev');
    		if ($addressSubmit)
    		{
	    		$formData = $this->getRequest()->getPost();
	    		//Zend_Debug::dump($formData);
	    		
	    		if ($formData['addressType']==1){
	    			$form->companyName->setRequired(false);
	    			$form->companyIC->setRequired(false);
	    			$formData['companyName'] = NULL;
	    			$formData['companyIC'] = NULL;
	    			$formData['companyDIC'] = NULL;
	    		}else{
    				$this->view->jQuery()->addOnLoad(
    					'$("#fieldset-billingInformation div.person").css({display: "none"});
    					 $("#fieldset-billingInformation div.company").css({display: "block"});');
	    			$form->personName->setRequired(false);
	    			$form->personSurname->setRequired(false);
	    			$formData['personTitle'] = NULL;
	    			$formData['personName'] = NULL;
	    			$formData['personSurname'] = NULL;
	    		}
	    		if ($formData['deliveryAddress']==1){
	    			$form->personName2->setRequired(false);
	    			$form->personSurname2->setRequired(false);
	    			$form->companyName2->setRequired(false);
	    			$form->companyIC2->setRequired(false);
	    			$form->email2->setRequired(false);
	    			$form->phone2->setRequired(false);
	    			$form->street2->setRequired(false);
	    			$form->street_nr2->setRequired(false);
	    			$form->city2->setRequired(false);
	    			$form->zip2->setRequired(false);
	    			$form->country2->setRequired(false);
	    			$formData['personName2'] = NULL;
	    			$formData['personSurname2'] = NULL;
	    			$formData['companyName2'] = NULL;
	    			$formData['companyIC2'] = NULL;
	    			$formData['email2'] = NULL;
	    			$formData['phone2'] = NULL;
	    			$formData['street2'] = NULL;
	    			$formData['street_nr2'] = NULL;
	    			$formData['city2'] = NULL;
	    			$formData['zip2'] = NULL;
	    			$formData['country2'] = NULL;
	    		}else{
	    			$this->view->jQuery()->addOnLoad('$("#fieldset-deliveryInformation").toggle();');
	    			
		    		if ($formData['addressType2']==1){
		    			$form->companyName2->setRequired(false);
		    			$form->companyIC2->setRequired(false);
		    			$formData['companyName2'] = NULL;
	    				$formData['companyIC2'] = NULL;
	    				$formData['companyDIC2'] = NULL;
		    		}else{
	    				$this->view->jQuery()->addOnLoad(
	    					'$("#fieldset-deliveryInformation div.person").css({display: "none"});
	    					 $("#fieldset-deliveryInformation div.company").css({display: "block"});');
		    			$form->personName2->setRequired(false);
		    			$form->personSurname2->setRequired(false);
		    			$formData['personTitle2'] = NULL;
	    				$formData['personName2'] = NULL;
	    				$formData['personSurname2'] = NULL;
		    		}
	    		}
	    		
	    		if ($form->isValid($formData))
	    		{
	    			$this->cart->addresses = $formData;
    				$this->_helper->redirector->gotoRoute(array(), 'cart_index_consumption-payment');
	    		}
    		}elseif ($prev){
    			$this->_helper->redirector->gotoRoute(array(), 'cart_index_index');
    		}
    	}else{
	    	if (isset($this->cart->addresses)){
	    		$addresses = $this->cart->addresses;
		    	//Zend_Debug::dump($addresses);
		    	$form->populate($addresses);
		    	
	    		if ($addresses['addressType']==2){
		    		$this->view->jQuery()->addOnLoad(
    					'$("#fieldset-billingInformation div.person").css({display: "none"});
    					 $("#fieldset-billingInformation div.company").css({display: "block"});');
		    	}
		    	
		    	if ($addresses['deliveryAddress']==0){
		    		$this->view->jQuery()->addOnLoad('$("#fieldset-deliveryInformation").toggle();');
		    	}
		    	
	    		if ($addresses['addressType2']==2){
		    		$this->view->jQuery()->addOnLoad(
	    				'$("#fieldset-deliveryInformation div.person").css({display: "none"});
	    				 $("#fieldset-deliveryInformation div.company").css({display: "block"});');
		    	}
	    	}
    	}
    	
    	
    }
    public function consumptionPaymentAction()
    {
    	$this->view->headLink()->prependStylesheet('/css/form.css');
    	
    	$this->_helper->layout()->setLayout('eshop');
    	$this->_helper->eshop->initLayout();
    	
    	$form = new Cart_Form_ConsumptionPayment();
    	$form->setAction($this->view->url(array(), 'cart_index_consumption-payment'));
    	$form->setAttrib('id', 'form-consumption-payment');
    	$this->view->form = $form;
    	
    	$paymentTab = new Cart_Model_DbTable_Payment();
    	$payment = $paymentTab->getPayment();
    	$options = array();
    	$options[''] = '-Vyberte ze seznamu-';
    	for ($i = 0; $i < count($payment); $i++) {
    		$options[$payment[$i]['id']] = $payment[$i]['payment'];
    	}
    	$form->payment->setMultiOptions($options);
    	
    	$consumptionTab = new Cart_Model_DbTable_Consumption();
    	$consumption = $consumptionTab->getConsumption();
    	$options = array();
    	$options[''] = '-Vyberte ze seznamu-';
    	for ($i = 0; $i < count($consumption); $i++) {
    		$options[$consumption[$i]['id']] = $consumption[$i]['consumption'];
    	}
    	$form->consumption->setMultiOptions($options);
    	
    	if ($this->getRequest()->isPost())
    	{
    		$prev = $this->getRequest()->getPost('prev');
    		if ($prev == NULL)
    		{
	    		$formData = $this->getRequest()->getPost();
	    		//Zend_Debug::dump($formData);
    			if ($form->isValid($formData))
	    		{
	    			$this->cart->consumptionpayment = $formData;
    				$this->_helper->redirector->gotoRoute(array(), 'cart_index_control-orders');
	    		}
    		}else{
    			$this->_helper->redirector->gotoRoute(array(), 'cart_index_addresses');
    		}
    	}else{
	    	if (isset($this->cart->consumptionpayment)){
	    		$consumptionpayment = $this->cart->consumptionpayment;
		    	//Zend_Debug::dump($addresses);
		    	$form->populate($consumptionpayment);
	    	}
	    }
    }
    public function controlOrdersAction()
    {
    	$this->view->headLink()->prependStylesheet('/css/table.css');
    	
    	$this->_helper->layout()->setLayout('eshop');
    	$this->_helper->eshop->initLayout();
    	
    	$consumptionpayment = $this->cart->consumptionpayment;
    	//Zend_Debug::dump($consumptionpayment);
    	$paymentTab = new Cart_Model_DbTable_Payment();
    	$payment = $paymentTab->getPaymentByID($consumptionpayment['payment']);
    	//Zend_Debug::dump($payment);
    	$this->view->payment = $payment;//[$consumptionpayment['payment']];
    	
    	$consumptionTab = new Cart_Model_DbTable_Consumption();
    	//$consumption = $consumptionTab->getConsumptionsPairs();
    	$consumption = $consumptionTab->getConsumptionByID($consumptionpayment['consumption']);
    	//Zend_Debug::dump($consumption);
    	$this->view->consumption = $consumption;//[$consumptionpayment['consumption']];
    	    	
    	$addresses = $this->cart->addresses;
    	//Zend_Debug::dump($addresses);
    	
    	if ($addresses['addressType']==1){
    		$billingInformation = $addresses['personTitle'].' '.$addresses['personName'].' '.$addresses['personSurname'].'<br />';
    	}else{
    		$billingInformation = $addresses['companyName'].', '.$addresses['companyIC'].(($addresses['companyDIC'])?', '.$addresses['companyDIC']:'').'<br />';
    	}
    	if ($addresses['deliveryAddress']==1){
    		$deliveryInformation = $billingInformation;
    	}else{
	    	if ($addresses['addressType2']==1){
	    		$deliveryInformation = $addresses['personTitle2'].' '.$addresses['personName2'].' '.$addresses['personSurname2'].'<br />';
	    	}else{
	    		$deliveryInformation = $addresses['companyName2'].', '.$addresses['companyIC2'].(($addresses['companyDIC2'])?', '.$addresses['companyDIC2']:'').'<br />';
	    	}
    	}
    	
    	$countriesTab = new Cart_Model_DbTable_Countries();
    	$countries = $countriesTab->getCountries();
    	for ($i = 0; $i < count($countries); $i++) {
    		$data[$countries[$i]['id']] = $countries[$i]['country'];
    	}
    	$countries = $data;
    	//Zend_Debug::dump($countries);
    	
    	$billingInformation .= $addresses['street'].' '.$addresses['street_nr'].', '.$addresses['zip'].' '.$addresses['city'].', '.$countries[$addresses['country']].'<br />';
    	$billingInformation .= ($addresses['contactPerson'])?'Kontaktní osoba '.$addresses['contactPerson'].', ':'';
    	$billingInformation .= 'e-mail '.$addresses['email'].', telefon '.$addresses['phone'];
    	$this->view->billingInformation = $billingInformation;
    	
    	if ($addresses['deliveryAddress']!=1){
	    	$deliveryInformation .= $addresses['street2'].' '.$addresses['street_nr2'].', '.$addresses['zip2'].' '.$addresses['city2'].', '.$countries[$addresses['country2']].'<br />';
	    	$deliveryInformation .= ($addresses['contactPerson2'])?'Kontaktní osoba '.$addresses['contactPerson2'].', ':'';
	    	$deliveryInformation .= 'e-mail '.$addresses['email2'].', telefon '.$addresses['phone2'];
	    	$this->view->deliveryInformation = $deliveryInformation;
	    }else{
    		$this->view->deliveryInformation = $billingInformation;
	    }
    	
    	$products = $this->cart->products;
    	//Zend_Debug::dump($products);
    	$this->view->products = $products;
    }
    public function ordersSentAction()
    {
    	if ($this->cart->products == NULL){
    		$this->_helper->redirector->gotoRoute(array(), 'cart_index_index');
    	}
    	$this->_helper->layout()->setLayout('eshop');
    	$this->_helper->eshop->initLayout();
    	
    	$consumptionpayment = $this->cart->consumptionpayment;
    	//Zend_Debug::dump($consumptionpayment);
    	$consumption_id = $consumptionpayment['consumption'];
    	$payment_id = $consumptionpayment['payment'];
    	
    	$ordersTab = new Cart_Model_DbTable_Orders();
    	$orders_id = $ordersTab->setOrder($consumption_id, $payment_id);
    	$this->view->orders_id = sprintf('%09d', $orders_id);
    	
    	$addresses = $this->cart->addresses;
    	//Zend_Debug::dump($addresses);
    	$addressesTab = new Cart_Model_DbTable_Addresses();
    	$billingAddress_id = $addressesTab->setAddress(
    		$addresses['street'], $addresses['street_nr'], $addresses['city'], $addresses['zip'], $addresses['country'],
    		$addresses['contactPerson'], $addresses['email'], $addresses['phone'],
			$addresses['personTitle'], $addresses['personName'], $addresses['personSurname'],
			$addresses['companyName'], $addresses['companyIC'], $addresses['companyDIC']
		);
    	if ($addresses['deliveryAddress']==1){
    		$deliveryAddress_id = $billingAddress_id;
    	}else{
	    	$deliveryAddress_id = $addressesTab->setAddress(
	    		$addresses['street2'], $addresses['street_nr2'], $addresses['city2'], $addresses['zip2'], $addresses['country2'],
	    		$addresses['contactPerson2'], $addresses['email2'], $addresses['phone2'],
				$addresses['personTitle2'], $addresses['personName2'], $addresses['personSurname2'],
				$addresses['companyName2'], $addresses['companyIC2'], $addresses['companyDIC2']
			);
    	}
    	
    	$orders2addressesTab = new Cart_Model_DbTable_Orders2Addresses();
    	$orders2addressesTab->setOrders2Address($orders_id, $billingAddress_id, 1, 0);
    	$orders2addressesTab->setOrders2Address($orders_id, $deliveryAddress_id, 0, 1);
    	    	
    	$products = $this->cart->products;
    	//Zend_Debug::dump($products);
    	$orders2productsTab = new Cart_Model_DbTable_Orders2Products();
    	foreach ($products as $product) {
    		$orders2productsTab->setOrders2Products($orders_id, $product['id'], $product['count']);
    	}
    	
    	if ($this->user){
    		$orders2UsersTab = new Cart_Model_DbTable_Orders2Users();
    		$orders2UsersTab->setOrders2Users($this->user['id'], $orders_id);
    	}
    	
    	$this->cart->unsetAll();
    	$this->view->cart = NULL;
    	
    	$this->createPDF($orders_id);
    	$this->view->pdf = sprintf('%09d', $orders_id).'.pdf';
    	
    	// Poslat email
        $settingsTab = new Admin_Model_DbTable_Settings();
        $smtp = $settingsTab->getFlag('smtp');
        //Zend_Debug::dump($smtp);
        $config = array(
        	'auth'		=>	'login',
			'username'	=>	$smtp['username'],
			'password'	=>	$smtp['password'],
			'ssl'		=>	$smtp['ssl'],
			'port'		=>	$smtp['port']
        );
		$transport = new Zend_Mail_Transport_Smtp($smtp['smtp'], $config);
		$eshop = $settingsTab->getFlag('eshop');
		$mail = new Zend_Mail('UTF-8');
		$mail->setHeaderEncoding(Zend_Mime::ENCODING_BASE64);
		$mail->setBodyHtml(
			'Vážený uživateli,<br />'.
			'vítáme Vás na serveru <a href="'.$eshop['url'].'">'.$eshop['title'].'</a>.<br />'.
			'<br />'.
			'Vaše objednávka byla úspěšně přijata.<br />'.
			'Objednávka je přiložena v příloze ve formátu PDF.'.
			'<br />'.
			'<br />'.
			'S přáním hezkého dne<br />'.
			'Web: <a href="http://'.$eshop['url'].'/">'.$eshop['title'].'</a>'.'<br />'.
			'Email: <a href="mailto:'.$eshop['email'].'">'.$eshop['email'].'</a>'
			,
			'UTF-8', 'UTF-8'
		);
		$mail->setFrom($eshop['email'], $eshop['title']);
		$mail->addTo($addresses['email'], $addresses['contactPerson']);
		$mail->setSubject("=?utf-8?B?".base64_encode($this->autoUTF ('Objednávka číslo: '.sprintf('%09d', $orders_id).' - '.$eshop['title']))."?=");
		$at = new Zend_Mime_Part(file_get_contents('data/pdf/orders/'.sprintf('%09d', $orders_id).'.pdf'));
		$at->type        = 'application/pdf';
		$at->disposition = Zend_Mime::DISPOSITION_ATTACHMENT;
		$at->filename    = sprintf('%09d', $orders_id).'.pdf';
		$mail->addAttachment($at);
		$mail->send($transport);
		
		// poslat email do eshopu
		$mail2 = new Zend_Mail('UTF-8');
		$mail2->setHeaderEncoding(Zend_Mime::ENCODING_BASE64);
		$mail2->setBodyHtml(
			'Vážený uživateli,<br />'.
			'byla uskutečněna nová objednávka na serveru <a href="'.$eshop['url'].'">'.$eshop['title'].'</a>.<br />'.
			'<br />'.
			'Objednávka je přiložena v příloze ve formátu PDF.'.
			'<br />'.
			'<br />'.
			'S přáním hezkého dne<br />'.
			'Web: <a href="http://'.$eshop['url'].'/">'.$eshop['title'].'</a>'.'<br />'.
			'Email: <a href="mailto:'.$eshop['email'].'">'.$eshop['email'].'</a>'
			,
			'UTF-8', 'UTF-8'
		);
		$mail2->setFrom($eshop['email'], $eshop['title']);
		$mail2->addTo($eshop['email'], $eshop['title']);
		$mail2->setSubject("=?utf-8?B?".base64_encode($this->autoUTF ('Objednávka číslo: '.sprintf('%09d', $orders_id).' - '.$eshop['title']))."?=");
		$mail2->addAttachment($at);
		$mail2->send($transport);
    }
	protected function createPDF($orders_id)
    {
    	$ordersTab = new Admin_Model_DbTable_Orders();
    	$orders = $ordersTab->getOrder($orders_id);
    	$date = new DateTime($orders['date']);
    	$orders['date'] = $date->format('d.m.Y');
    	//Zend_Debug::dump($orders);
    	
    	$paymentTab = new Cart_Model_DbTable_Payment();
    	$payment = $paymentTab->getPaymentByID($orders['payment_id']);
    	//$payment = $payment['payment'];
    	//Zend_Debug::dump($payment);
    	
    	$consumptionTab = new Cart_Model_DbTable_Consumption();
    	$consumption = $consumptionTab->getConsumptionByID($orders['consumption_id']);
    	//$consumption = $consumption['consumption'];
    	//Zend_Debug::dump($consumption);
    	
    	$addressesTab = new Cart_Model_DbTable_Addresses();
    	$billingAddress = $addressesTab->getBillingAddressesByOrderID($orders_id);
    	//Zend_Debug::dump($billingAddress);
    	$deliveryAddress = $addressesTab->getDeliveryAddressesByOrderID($orders_id);
    	//Zend_Debug::dump($deliveryAddress);
    	
    	$settingsTab = new Cart_Model_DbTable_Settings();
    	$eshop = $settingsTab->getFlag('eshop');
    	//Zend_Debug::dump($eshop);
    	$bank = $settingsTab->getFlag('bank');
    	//Zend_Debug::dump($bank);
    	$company = $settingsTab->getFlag('company');
    	//Zend_Debug::dump($company);
    	
    	$orders2productsTab = new Admin_Model_DbTable_Orders2Products();
    	$products = $orders2productsTab->getProductsByOrderID($orders_id);
    	//Zend_Debug::dump($products);
    	
    	//-----------------------------------------------------------------------------------------
    	$pdf = new Zend_Pdf();
		$page = new Zend_Pdf_Page(Zend_Pdf_Page::SIZE_LETTER);
		$pageHeight = $page->getHeight();
		$pageWidth = $page->getWidth();

		$pdf->properties['Title'] = $this->Convert('Objednávka číslo: ').sprintf('%09d', $orders_id);
		$pdf->properties['Author'] = $this->Convert('www.eshop.rogr.cz');
		$pdf->properties['Subject'] = $this->Convert('Objednávka');
		$pdf->properties['Keywords'] = $this->Convert('Objednávka');
		$pdf->properties['Creator'] = $this->Convert('www.eshop.rogr.cz');
		$pdf->properties['Producer'] = $this->Convert('www.eshop.rogr.cz');
		$pdf->properties['CreationDate'] = 'D:'.date("YmdHis+01'00'");
		$pdf->properties['ModDate'] = 'D:'.date("YmdHis+01'00'");
		$pdf->properties['Trapped'] = false;
		
		$pdf->pages[0] = ($page);
		
		$style = new Zend_Pdf_Style();
		$style->setLineColor(new Zend_Pdf_Color_Rgb(0,0,0));
		$style->setLineWidth(1);
		$page->setStyle($style);
		//-----------------------------------------------------------------------------------------
		$page->drawRectangle(30, $pageHeight - 30, $pageWidth - 30, 30, Zend_Pdf_Page::SHAPE_DRAW_STROKE);
		
		$page->drawLine(20, $pageHeight/3, 30, $pageHeight/3);
		$page->drawLine($pageWidth - 30, $pageHeight/3, $pageWidth - 20, $pageHeight/3);
		$page->drawLine(20, ($pageHeight/3)*2, $pageWidth - 20, ($pageHeight/3)*2);
		$page->drawLine($pageWidth/2, $pageHeight-30, $pageWidth/2, ($pageHeight/3)*2);
		$page->drawLine(30, 50, $pageWidth - 30, 50);
		$page->drawLine(30, $pageHeight - 285, $pageWidth - 30, $pageHeight - 285);
		//-----------------------------------------------------------------------------------------
		$style->setLineWidth(2);
		$page->setStyle($style);
		$page->drawRectangle($pageWidth/2, $pageHeight - 30, $pageWidth - 30, $pageHeight - 156, Zend_Pdf_Page::SHAPE_DRAW_STROKE);
		//-----------------------------------------------------------------------------------------
		$font = Zend_Pdf_Font::fontWithName(Zend_Pdf_Font::FONT_HELVETICA);
		$fontBold = Zend_Pdf_Font::fontWithName(Zend_Pdf_Font::FONT_HELVETICA_BOLD);
		//-----------------------------------------------------------------------------------------
		// BOLD MODRY 14 **************************************************************************
		$style->setFont($fontBold,14);
		$style->setFillColor(new Zend_Pdf_Color_Rgb(0,0,128));
		$page->setStyle($style);
		
		$text = $this->Convert('POTVRZENÍ PŘIJETÍ OBJEDNÁVKY');
		$page->drawText($text ,$pageWidth - 270, $pageHeight - 25, 'UTF-8');
		// ****************************************************************************************
		//BOLD CERNY 14 ***************************************************************************
		$style->setFillColor(new Zend_Pdf_Color_Rgb(0,0,0));
		$page->setStyle($style);
		
		$text = $this->Convert($eshop['title']);
		$page->drawText($text, 30, $pageHeight - 25, 'UTF-8');
		// ****************************************************************************************
		//BOLD CERNY 11 ***************************************************************************
		$style->setFont($fontBold,11);
		$style->setFillColor(new Zend_Pdf_Color_Rgb(0,0,0));
		$page->setStyle($style);
		
		$text = $this->Convert($eshop['title']);
		$page->drawText($text , 40, $pageHeight - 70, 'UTF-8');
		$text = $this->Convert($eshop['street'].' '.$eshop['street_nr']);
		$page->drawText($text, 40, $pageHeight - 85, 'UTF-8');
		$text = $this->Convert($eshop['zip'].' '.$eshop['city']);
		$page->drawText($text, 40, $pageHeight - 100, 'UTF-8');
		$text = $this->Convert($eshop['country']);
		$page->drawText($text, 40, $pageHeight - 115, 'UTF-8');
		
		$text = $this->Convert(($billingAddress['company_name'])?$billingAddress['company_name']:($billingAddress['person_title'].' '.$billingAddress['person_name'].' '.$billingAddress['person_surname']));
		$page->drawText($text, 315, $pageHeight - 65, 'UTF-8');
		$text = $this->Convert($billingAddress['street'].' '.$billingAddress['street_nr']);
		$page->drawText($text, 315, $pageHeight - 78, 'UTF-8');
		$text = $this->Convert($billingAddress['zip']).' '.$billingAddress['city'];
		$page->drawText($text, 315, $pageHeight - 91, 'UTF-8');
		$text = $this->Convert($billingAddress['country']);
		$page->drawText($text, 315, $pageHeight - 104, 'UTF-8');
		// ****************************************************************************************
		
		//BOLD CERNY 9 ***************************************************************************
		$style->setFont($fontBold,9);
		$page->setStyle($style);
		
		$text = $this->Convert(($deliveryAddress['company_name'])?$deliveryAddress['company_name']:($deliveryAddress['person_title'].' '.$deliveryAddress['person_name'].' '.$deliveryAddress['person_surname']));
		$page->drawText($text, 315, $pageHeight - 190, 'UTF-8');
		$text = $this->Convert($deliveryAddress['street'].' '.$deliveryAddress['street_nr']);
		$page->drawText($text, 315, $pageHeight - 200, 'UTF-8');
		$text = $this->Convert($deliveryAddress['zip'].' '.$deliveryAddress['city']);
		$page->drawText($text, 315, $pageHeight - 210, 'UTF-8');
		$text = $this->Convert($deliveryAddress['country']);
		$page->drawText($text, 315, $pageHeight - 220, 'UTF-8');
		// ****************************************************************************************
		
		//NORMAL MODRY 10 *************************************************************************
		$style->setFont($font,10);
		$style->setFillColor(new Zend_Pdf_Color_Rgb(0,0,128));
		$page->setStyle($style);
		
		$text = $this->Convert('Dodavatel:');
		$page->drawText($text, 40, $pageHeight - 45, 'UTF-8');
		$text = $this->Convert('IČ: '.$eshop['ic']);
		$page->drawText($text, 175, $pageHeight - 45, 'UTF-8');
		$text = $this->Convert('DIČ: '.$eshop['dic']);
		$page->drawText($text, 175, $pageHeight - 56, 'UTF-8');
		
		$text = $this->Convert('Odběratel:');
		$page->drawText($text, 315, $pageHeight - 45, 'UTF-8');
		$text = $this->Convert('IČ: '.$billingAddress['company_identification']);
		$page->drawText($text, 450, $pageHeight - 45, 'UTF-8');
		$text = $this->Convert('DIČ: '.$billingAddress['company_vat']);
		$page->drawText($text, 450, $pageHeight - 57, 'UTF-8');
		// ****************************************************************************************
		
		//NORMAL MODRY 9 *************************************************************************
		$style->setFont($font,9);
		$page->setStyle($style);
		
		$text = $this->Convert('Konečný odběratel:');
		$page->drawText($text, 315, $pageHeight - 173, 'UTF-8');
		$text = $this->Convert('IČ: '.$deliveryAddress['company_identification']);
		$page->drawText($text, 450, $pageHeight - 173, 'UTF-8');
		$text = $this->Convert('DIČ: '.$deliveryAddress['company_vat']);
		$page->drawText($text, 450, $pageHeight - 183, 'UTF-8');
		// ****************************************************************************************
		
		//NORMAL CERNY 10 *************************************************************************
		$style->setFillColor(new Zend_Pdf_Color_Rgb(0,0,0));
		$page->setStyle($style);
		
		$text = $this->Convert('Bankovní spojení:');
		$page->drawText($text, 40, $pageHeight - 139, 'UTF-8');
		$text = $this->Convert('Číslo účtu: '.$bank['account']);
		$page->drawText($text, 40, $pageHeight - 151, 'UTF-8');
		$text = $this->Convert('Název banky: '.$bank['title']);
		$page->drawText($text, 40, $pageHeight - 163, 'UTF-8');
		$text = $this->Convert('IBAN: '.$bank['iban']);
		$page->drawText($text, 40, $pageHeight - 175, 'UTF-8');
		$text = $this->Convert('BIC: '.$bank['bic']);
		$page->drawText($text, 40, $pageHeight - 187, 'UTF-8');
		
		$text = $this->Convert('Telefon: '.$eshop['phone']);
		$page->drawText($text, 40, 70, 'UTF-8');
		$text = $this->Convert('Mobil: '.$eshop['mobile']);
		$page->drawText($text, 40, 58, 'UTF-8');
		$text = $this->Convert('Fax: '.$eshop['fax']);
		$page->drawText($text, 260, 70, 'UTF-8');
		$text = $this->Convert('E-mail: '.$eshop['email']);
		$page->drawText($text, 260, 58, 'UTF-8');
		$text = $this->Convert('Web: '.$eshop['url']);
		$page->drawText($text, 440, 58, 'UTF-8');
		
		$text = $this->Convert($company['registration']);
		$page->drawText($text, 40, 37, 'UTF-8');
		$text = 'www.eshop.rogr.cz';
		$page->drawText($text, ($pageWidth/2) - 40, 20, 'UTF-8');
		
		$text = $this->Convert('Kontaktní osoba: '.$billingAddress['contact_person']);
		$page->drawText($text, 315, $pageHeight - 122, 'UTF-8');
		$text = $this->Convert('Kontaktní email: '.$billingAddress['contact_email']);
		$page->drawText($text, 315, $pageHeight - 134, 'UTF-8');
		$text = $this->Convert('Kontaktní telefon: '.$billingAddress['contact_phone']);
		$page->drawText($text, 315, $pageHeight - 146, 'UTF-8');
		// ****************************************************************************************
		
		//NORMAL CERNY 9 **************************************************************************
		$style->setFont($font,9);
		$page->setStyle($style);
		
		$text = $this->Convert('Kontaktní osoba: '.$deliveryAddress['contact_person']);
		$page->drawText($text, 315, $pageHeight - 235, 'UTF-8');
		$text = $this->Convert('Kontaktní email: '.$deliveryAddress['contact_email']);
		$page->drawText($text, 315, $pageHeight - 245, 'UTF-8');
		$text = $this->Convert('Kontaktní telefon: '.$deliveryAddress['contact_phone']);
		$page->drawText($text, 315, $pageHeight - 255, 'UTF-8');
		// ****************************************************************************************
		
		//NORMAL CERNY 11 *************************************************************************
		$style->setFont($font,11);
		$style->setFillColor(new Zend_Pdf_Color_Rgb(0,0,0));
		$page->setStyle($style);
		
		$text = $this->Convert('Objednávka č.:');
		$page->drawText($text, 40, $pageHeight - 210, 'UTF-8');
		$text = sprintf('%09d', $orders_id);
		$page->drawText($text, 140, $pageHeight - 210, 'UTF-8');
		$text = $this->Convert('Způsob odběru:');
		$page->drawText($text, 40, $pageHeight - 225, 'UTF-8');
		$text = $this->Convert($consumption['consumption']);
		$page->drawText($text, 140, $pageHeight - 225, 'UTF-8');
		$text = $this->Convert('Způsob úhrady:');
		$page->drawText($text, 40, $pageHeight - 240, 'UTF-8');
		$text = $this->Convert($payment['payment']);
		$page->drawText($text, 140, $pageHeight - 240, 'UTF-8');
		$text = $this->Convert('Datum zápisu:');
		$page->drawText($text, 40, $pageHeight - 255, 'UTF-8');
		$text = $orders['date'];
		$page->drawText($text, 140, $pageHeight - 255, 'UTF-8');
		
		$text = $this->Convert('Kód položky');
		$page->drawText($text, 40, $pageHeight - 280, 'UTF-8');
		$text = $this->Convert('Název');
		$page->drawText($text, 150, $pageHeight - 280, 'UTF-8');
		$text = $this->Convert('Množství');
		$page->drawText($text, 300, $pageHeight - 280, 'UTF-8');
		$text = $this->Convert('DPH');
		$page->drawText($text, 370, $pageHeight - 280, 'UTF-8');
		$text = $this->Convert('Bez DPH/j');
		$page->drawText($text, 420, $pageHeight - 280, 'UTF-8');
		$text = $this->Convert('Bez DPH');
		$page->drawText($text, 500, $pageHeight - 280, 'UTF-8');
		
		// ZBOZI **********************************************************************************
		$totalPrice = 0;
		$totalPriceVAT = 0;
		
		for ($i=0; $i<count($products); $i++) {
			$text = $this->Convert($products[$i]['code']);
			$page->drawText($text, 40, ($pageHeight - 300) - (30 * $i), 'UTF-8');
			$text = $this->Convert($products[$i]['title']);
			$page->drawText($text, 150, ($pageHeight - 300) - (30 * $i), 'UTF-8');
			$text = $products[$i]['count'].' ks';
			$page->drawText($text, 300, ($pageHeight - 300) - (30 * $i), 'UTF-8');
			$text = $products[$i]['vat_title'];
			$page->drawText($text, 370, ($pageHeight - 300) - (30 * $i), 'UTF-8');
			$text = number_format($products[$i]['price_vat'], 0, '.', ' ').' '.$this->Convert($eshop['currency']);
			$page->drawText($text, 420, ($pageHeight - 300) - (30 * $i), 'UTF-8');
			$text = number_format(($products[$i]['price_vat']*$products[$i]['count']), 0, '.', ' ').' '.$this->Convert($eshop['currency']);
			$page->drawText($text, 500, ($pageHeight - 300) - (30 * $i), 'UTF-8');
			
			$totalPriceVAT += $products[$i]['price_vat'] * $products[$i]['count'];
			$totalPrice += $products[$i]['price'] * $products[$i]['count'];
		}
		
		$text = $this->Convert('Celková cena bez DPH:');
		$page->drawText($text, 300, ($pageHeight - 300) - (30 * $i)-20, 'UTF-8');
		$text = number_format($totalPriceVAT, 0, '.', ' ').' '.$this->Convert($eshop['currency']);
		$page->drawText($text, 500, ($pageHeight - 300) - (30 * $i)-20, 'UTF-8');
		$text = $this->Convert('DPH:');
		$page->drawText($text, 300, ($pageHeight - 300) - (30 * $i)-35, 'UTF-8');
		$text = number_format($totalPrice-$totalPriceVAT, 0, '.', ' ').' '.$this->Convert($eshop['currency']);
		$page->drawText($text, 500, ($pageHeight - 300) - (30 * $i)-35, 'UTF-8');
		
		$totalPrice += $orders['cons_price'];
		$totalPrice += $orders['pay_price'];
		// ****************************************************************************************
		
		//BOLD CERNY 12 ***************************************************************************
		$style->setFont($fontBold,12);
		$page->setStyle($style);
		
		$text = $this->Convert('Celková cena s DPH:');
    	$page->drawText($text, 300, ($pageHeight - 300) - (30 * $i)-80, 'UTF-8');
    	$text = number_format($totalPrice, 0, '.', ' ').' '.$this->Convert($eshop['currency']);
		$page->drawText($text, 500, ($pageHeight - 300) - (30 * $i)-80, 'UTF-8');
		// ****************************************************************************************
		
		//BOLD CERNY 9 ***************************************************************************
		$style->setFont($font,9);
		$page->setStyle($style);
		
    	for ($i=0; $i<count($products); $i++) {
    		$text = $this->Convert($products[$i]['short_desc']);
			$page->drawText($text, 40, ($pageHeight - 300) - (30 * $i) -12, 'UTF-8');
		}
		if ($orders['cons_price']){
    		$text = $this->Convert('Dopravné s DPH:');
			$page->drawText($text, 300, ($pageHeight - 300) - (30 * $i)-47, 'UTF-8');
			$text = number_format($orders['cons_price'], 0, '.', ' ').' '.$this->Convert($eshop['currency']);
			$page->drawText($text,500, ($pageHeight - 300) - (30 * $i)-47, 'UTF-8');
		}
    	if ($orders['pay_price']){
    		$text = $this->Convert('Doběrečné s DPH:');
			$page->drawText($text, 300, ($pageHeight - 300) - (30 * $i)-59, 'UTF-8');
			$text = number_format($orders['pay_price'], 0, '.', ' ').' '.$this->Convert($eshop['currency']);
			$page->drawText($text,500, ($pageHeight - 300) - (30 * $i)-59, 'UTF-8');
		}
		// ****************************************************************************************
		
		// ULOZENI ********************************************************************************
		$pdf->save('data/pdf/orders/'.sprintf('%09d', $orders_id).'.pdf');
	}
    /**
     * Return converted text
     * @param	string	$text
     * @return	string
     */
	private function Convert($text)
	{
		$ar = array(//' '=> '-', '&'=>'-', ':'=>'-', '.'=>'-', ','=>'-', '%'=>'-', '('=>'-', ')'=>'-',
					'á'=> 'a', 'č'=> 'c', 'ď'=> 'd', 'é'=> 'e', 'ě'=> 'e', 'í'=> 'i', 'ň'=> 'n', 'ó'=> 'o',
					'ř'=> 'r', 'š'=> 's', 'ť'=> 't', 'ú'=> 'u', 'ů'=> 'u', 'ý'=> 'y', 'ž'=> 'z',
					'Á'=> 'A', 'Č'=> 'C', 'Ď'=> 'D', 'É'=> 'E', 'Ě'=> 'E', 'Í'=> 'I', 'Ň'=> 'N', 'Ó'=> 'O',
					'Ř'=> 'R', 'Š'=> 'S', 'Ť'=> 'T', 'Ú'=> 'U', 'Ů'=> 'U', 'Ý'=> 'Y', 'Ž'=> 'Z' );
		
		foreach ($ar as $key=>$value) {
			$text = str_replace($key, $value, $text);
		}		
		return $text;
	}
	
	private function autoUTF($s)
	{
	    // detect UTF-8
	    if (preg_match('#[\x80-\x{1FF}\x{2000}-\x{3FFF}]#u', $s))
	        return $s;
	    // detect WINDOWS-1250
	    if (preg_match('#[\x7F-\x9F\xBC]#', $s))
	        return iconv('WINDOWS-1250', 'UTF-8', $s);
	    // assume ISO-8859-2
	    return iconv('ISO-8859-2', 'UTF-8', $s);
	}
}