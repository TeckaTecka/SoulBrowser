<?php

class Admin_Orders_OrdersController extends Zend_Controller_Action
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
    		->prependStylesheet('/css/shared/paginator.css');
    	$this->view->jQuery()->addOnLoad(
    		'$("span a[title], span[title]").tooltip({
    			effect: "fade",
    			position: "top center",
    			opacity: 0.8,
    			predelay: 1000
    		 });
    	');
    	
    	$page = $this->_getParam('page');
    	
    	$ordersTab = new Admin_Model_DbTable_Orders();
    	$orders = $ordersTab->getOrders($page);
    	$this->view->orders = $orders;
    	//Zend_Debug::dump($orders);
    	
    	$db = Zend_Registry::get('db');
    	$adapter = new Zend_Paginator_Adapter_DbSelect(
    		$db->select()
    			->from('orders')
    	);
    	$paginator = new Zend_Paginator($adapter);
    	$paginator->setItemCountPerPage(20)
    		->setCurrentPageNumber($page);
    	//Zend_Debug::dump($paginator);
    	$this->view->paginator = $paginator;
    }
    public function editAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css')
    		->prependStylesheet('/css/shared/table.css')
    		->prependStylesheet('/css/admin/orders.css');
    	$this->view->jQuery()->addOnLoad(
    		//tabs
        	'$("#summary").click(function(){
        		$("#tab1").css({display: "block"});$("#summary").addClass("active");
    		 	$("#tab2").css({display: "none"});$("#general").removeClass("active");
    		 	$("#tab3").css({display: "none"});$("#addresses").removeClass("active");
    			$("#tab4").css({display: "none"});$("#products").removeClass("active");
    			$("#tab5").css({display: "none"});$("#status").removeClass("active");
    		 });
    		 $("#general").click(function(){
    			$("#tab1").css({display: "none"});$("#summary").removeClass("active");
    			$("#tab2").css({display: "block"});$("#general").addClass("active");
    		 	$("#tab3").css({display: "none"});$("#addresses").removeClass("active");
    			$("#tab4").css({display: "none"});$("#products").removeClass("active");
    			$("#tab5").css({display: "none"});$("#status").removeClass("active");
    		 });
    		 $("#addresses").click(function(){
    			$("#tab1").css({display: "none"});$("#summary").removeClass("active");
    			$("#tab2").css({display: "none"});$("#general").removeClass("active");
    		 	$("#tab3").css({display: "block"});$("#addresses").addClass("active");
    			$("#tab4").css({display: "none"});$("#products").removeClass("active");
    			$("#tab5").css({display: "none"});$("#status").removeClass("active");
    		 });
    		 $("#products").click(function(){
    			$("#tab1").css({display: "none"});$("#summary").removeClass("active");
    			$("#tab2").css({display: "none"});$("#general").removeClass("active");
    		 	$("#tab3").css({display: "none"});$("#addresses").removeClass("active");
    			$("#tab4").css({display: "block"});$("#products").addClass("active");
    			$("#tab5").css({display: "none"});$("#status").removeClass("active");
    		 });
    		 $("#status").click(function(){
    			$("#tab1").css({display: "none"});$("#summary").removeClass("active");
    			$("#tab2").css({display: "none"});$("#general").removeClass("active");
    		 	$("#tab3").css({display: "none"});$("#addresses").removeClass("active");
    			$("#tab4").css({display: "none"});$("#products").removeClass("active");
    			$("#tab5").css({display: "block"});$("#status").addClass("active");
    		 });'.
    		 //tab3
    		 '$("#billing").click(function(){
        		$("#tab3-1").css({display: "block"});$("#billing").addClass("active");
    		 	$("#tab3-2").css({display: "none"});$("#delivery").removeClass("active");
    		 });
    		 $("#delivery").click(function(){
    			$("#tab3-1").css({display: "none"});$("#billing").removeClass("active");
    		 	$("#tab3-2").css({display: "block"});$("#delivery").addClass("active");
    		 });
    		 $("#tab3-1 #customer").click(function(){
        		$("#tab3-1 div.person").css({display: "block"});$("#tab3-1 #customer").addClass("active");
    		 	$("#tab3-1 div.company").css({display: "none"});$("#tab3-1 #company").removeClass("active");
    		 	$("#tab3-1 #companyName").val("");$("#tab3-1 #companyIC").val("");$("#tab3-1 #companyDIC").val("");
    		 });
    		 $("#tab3-1 #company").click(function(){
    			$("#tab3-1 div.person").css({display: "none"});$("#tab3-1 #customer").removeClass("active");
    		 	$("#tab3-1 div.company").css({display: "block"});$("#tab3-1 #company").addClass("active");
    		 	$("#tab3-1 #personTitle").val("");$("#tab3-1 #personName").val("");$("#tab3-1 #personSurname").val("");
    		 });
    		 $("#tab3-2 #customer").click(function(){
        		$("#tab3-2 div.person").css({display: "block"});$("#tab3-2 #customer").addClass("active");
    		 	$("#tab3-2 div.company").css({display: "none"});$("#tab3-2 #company").removeClass("active");
    		 	$("#tab3-2 #companyName").val("");$("#tab3-2 #companyIC").val("");$("#tab3-2 #companyDIC").val("");
    		 });
    		 $("#tab3-2 #company").click(function(){
    			$("#tab3-2 div.person").css({display: "none"});$("#tab3-2 #customer").removeClass("active");
    		 	$("#tab3-2 div.company").css({display: "block"});$("#tab3-2 #company").addClass("active");
    		 	$("#tab3-2 #personTitle").val("");$("#tab3-2 #personName").val("");$("#tab3-2 #personSurname").val("");
    		 });
    	');
    	// PREHLED ********************************************************************************
    	$id = $this->_getParam('id');
    	$back = $this->_getParam('back');
    	
    	if ($back == 1){
    		$this->view->jQuery()->addOnLoad('$("#products").click();');
    	}
    	
    	$ordersTab = new Admin_Model_DbTable_Orders();
    	$order = $ordersTab->getOrder($id);
    	//Zend_Debug::dump($order);
    	$this->view->order = $order;
    	
    	$addressesTab = new Admin_Model_DbTable_Addresses();
    	$billingAddress = $addressesTab->getBillingAddress($id);
    	//Zend_Debug::dump($billingAddress);
    	$this->view->billingAddress = $billingAddress;
    	$deliveryAddress = $addressesTab->getDeliveryAddress($id);
    	//Zend_Debug::dump($deliveryAddress);
    	$this->view->deliveryAddress = $deliveryAddress;
    	
    	$productsTab = new Admin_Model_DbTable_Products();
    	$products = $productsTab->getProductsByOrderID($id);
    	//Zend_Debug::dump($products);
    	$this->view->products = $products;
    	
    	$this->view->currency = $productsTab->getCurrency();
    	//*****************************************************************************************
    	// OBECNE *********************************************************************************
    	$formGeneral = new Admin_Form_Orders_Orders_General();
    	$this->view->formGeneral = $formGeneral;
    	
    	
    	
    	// ****************************************************************************************
    	// ADRESY *********************************************************************************
    	$formBilling = new Admin_Form_Orders_Orders_BillingAddress();
    	$formBilling->setAction($this->view->url(array(), 'admin_orders_orders-edit'));
    	$formBilling->setAttrib('id', 'form-billing-address');
    	$this->view->formBilling = $formBilling;
    	
    	$formDelivery = new Admin_Form_Orders_Orders_DeliveryAddress();
    	$formDelivery->setAction($this->view->url(array(), 'admin_orders_orders-edit'));
    	$formDelivery->setAttrib('id', 'form-delivery-address');
    	$this->view->formDelivery = $formDelivery;
    	
    	$countriesTab = new Admin_Model_DbTable_Countries();
    	$countries = $countriesTab->getCountriesPairs();
    	$formBilling->country->setMultiOptions($countries);
    	$formDelivery->country->setMultiOptions($countries);
    	//*****************************************************************************************
    	
    	if ($this->getRequest()->isPost() AND $this->getRequest()->getPost('saveGeneral'))
    	{
    		$this->view->jQuery()->addOnLoad('$("#general").click();');
    		$formData = $this->getRequest()->getPost();
    		//Zend_Debug::dump($formData);
    		if ($order['orders_statuses_id'] <> $formData['status']){
	    		$statusLogTab = new Admin_Model_DbTable_StatusesLog();
	    		$statusLogTab->setLog(
	    			$id,
	    			$order['orders_statuses_id'],
	    			$formData['status']
	    		);
    		}
    		$ordersTab->updateOrder(
    			$id,
    			$formData['consumption'],
    			$formData['payment'],
    			$formData['status']
    		);
    		$order = $ordersTab->getOrder($id);
    		$this->view->order = $order;
    		
    		$this->createPDF($id);
    	}
    	elseif ($this->getRequest()->isPost() AND
			(($this->getRequest()->getPost('saveBilling')) OR ($this->getRequest()->getPost('saveDelivery'))))
    	{
    		$this->view->jQuery()->addOnLoad('$("#addresses").click();');
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
			$saveBilling = $this->getRequest()->getPost('saveBilling');
	    	if ($saveBilling)
	    	{
	    		if ($formData['companyName'])
	    		{
	    			$formBilling->personName->setRequired(false);
		    		$formBilling->personSurname->setRequired(false);
		    		$formData['personTitle'] = NULL;
		    		$formData['personName'] = NULL;
		    		$formData['personSurname'] = NULL;
		    		$this->view->jQuery()->addOnLoad('$("#tab3-1 #company").click();');
	    		}else{
	    			$formBilling->companyName->setRequired(false);
	    			$formBilling->companyIC->setRequired(false);
	    			$formData['companyName'] = NULL;
	    			$formData['companyIC'] = NULL;
	    			$formData['companyDIC'] = NULL;
	    			$this->view->jQuery()->addOnLoad('$("#tab3-1 #customer").click();');
	    		}
	    		if ($formBilling->isValid($formData))
	    		{
	    			$addressesTab->updateAddress(
	    			$billingAddress['id'], $formData['street'], $formData['street_nr'], $formData['city'],
	    			$formData['zip'], $formData['country'], $formData['contactPerson'], $formData['email'],
	    			$formData['phone'], $formData['personTitle'], $formData['personName'], $formData['personSurname'],
	    			$formData['companyName'], $formData['companyIC'], $formData['companyDIC']);
	    			
	    			$billingAddress = $addressesTab->getBillingAddress($id);
		    		$this->view->billingAddress = $billingAddress;
		    		
		    		$this->createPDF($id);
	    		}
	    	}
	    	
	    	$saveDelivery = $this->getRequest()->getPost('saveDelivery');
	    	if ($saveDelivery)
	    	{
	    		$this->view->jQuery()->addOnLoad('$("#delivery").click();');
	    		if ($formData['companyName'])
	    		{
	    			$formDelivery->personName->setRequired(false);
		    		$formDelivery->personSurname->setRequired(false);
		    		$formData['personTitle'] = NULL;
		    		$formData['personName'] = NULL;
		    		$formData['personSurname'] = NULL;
		    		$this->view->jQuery()->addOnLoad('$("#tab3-2 #company").click();');
	    		}else{
	    			$formDelivery->companyName->setRequired(false);
	    			$formDelivery->companyIC->setRequired(false);
	    			$formData['companyName'] = NULL;
	    			$formData['companyIC'] = NULL;
	    			$formData['companyDIC'] = NULL;
	    		}
	    		if ($formDelivery->isValid($formData))
	    		{
	    			$addressesTab->updateAddress(
	    			$deliveryAddress['id'], $formData['street'], $formData['street_nr'], $formData['city'],
	    			$formData['zip'], $formData['country'], $formData['contactPerson'], $formData['email'],
	    			$formData['phone'], $formData['personTitle'], $formData['personName'], $formData['personSurname'],
	    			$formData['companyName'], $formData['companyIC'], $formData['companyDIC']);
	    			
	    			$deliveryAddress = $addressesTab->getdeliveryAddress($id);
		    		$this->view->deliveryAddress = $deliveryAddress;
		    		
		    		$this->createPDF($id);
	    		}
	    	}
    	}
    	
    	// GENERAL ****************************************************************************
    	$paymentsTab = new Admin_Model_DbTable_Payments();
    	$payments = $paymentsTab->getPaymentsPairs();
    	$formGeneral->payment->setMultiOptions($payments);
    	$formGeneral->payment->setValue($order['payment_id']);
    	$statusesTab = new Admin_Model_DbTable_OrdersStatuses();
    	$statuses = $statusesTab->getStatusesPairs();
    	$formGeneral->status->setMultiOptions($statuses);
    	$formGeneral->status->setValue($order['orders_statuses_id']);
	    $consumptionTab = new Admin_Model_DbTable_Consumptions();
    	$consumptions = $consumptionTab->getConsumptionsPairs();
    	$formGeneral->consumption->setMultiOptions($consumptions);
    	$formGeneral->consumption->setValue($order['consumption_id']);
    	
    	$statusLogTab = new Admin_Model_DbTable_StatusesLog();
	    $statusesLogs = $statusLogTab->getStatusesLogsByOrderID($id);
	    //Zend_Debug::dump($statusesLogs);
	    $this->view->statuses = $statuses;
    	$this->view->statusesLogs = $statusesLogs;
	    // ADRESY *****************************************************************************
    	$billing = array(
	    	'personTitle'		=>	$billingAddress['person_title'],
	    	'personName'		=>	$billingAddress['person_name'],
	    	'personSurname'		=>	$billingAddress['person_surname'],
	    	'companyName'		=>	$billingAddress['company_name'],
	    	'companyIC'			=>	$billingAddress['company_identification'],
	    	'companyDIC'		=>	$billingAddress['company_vat'],
	    	'contactPerson'		=>	$billingAddress['contact_person'],
	    	'email'				=>	$billingAddress['contact_email'],
	    	'phone'				=>	$billingAddress['contact_phone'],
	    	'street'			=>	$billingAddress['street'],
	    	'street_nr'			=>	$billingAddress['street_nr'],
	    	'city'				=>	$billingAddress['city'],
	    	'zip'				=>	$billingAddress['zip'],
	    	'country'			=>	$billingAddress['countries_id'],
	    );
		//Zend_Debug::dump($addresses);
		$formBilling->populate($billing);
		   
		$delivery = array(
	    	'personTitle'		=>	$deliveryAddress['person_title'],
	    	'personName'		=>	$deliveryAddress['person_name'],
	    	'personSurname'		=>	$deliveryAddress['person_surname'],
	    	'companyName'		=>	$deliveryAddress['company_name'],
	    	'companyIC'			=>	$deliveryAddress['company_identification'],
	    	'companyDIC'		=>	$deliveryAddress['company_vat'],
	    	'contactPerson'		=>	$deliveryAddress['contact_person'],
	    	'email'				=>	$deliveryAddress['contact_email'],
	    	'phone'				=>	$deliveryAddress['contact_phone'],
	    	'street'			=>	$deliveryAddress['street'],
	    	'street_nr'			=>	$deliveryAddress['street_nr'],
	    	'city'				=>	$deliveryAddress['city'],
	    	'zip'				=>	$deliveryAddress['zip'],
	    	'country'			=>	$deliveryAddress['countries_id'],
	    );
		$formDelivery->populate($delivery);
    	
    	
    	if ($billingAddress['company_name']){
    		$this->view->jQuery()->addOnLoad('$("#tab3-1 #company").click();');
    	}else{
			$this->view->jQuery()->addOnLoad('$("#tab3-1 #customer").click();');
    	}
    	if ($deliveryAddress['company_name']){
    		$this->view->jQuery()->addOnLoad('$("#tab3-2 #company").click();');
    	}else{
			$this->view->jQuery()->addOnLoad('$("#tab3-2 #customer").click();');
    	}
    }
    public function addProductAction()
    {
    	$id = $this->_getParam('id');
    	$product_id = $this->_getParam('product_id');
    	$back = $this->_getParam('back');
    	
    	$orders2productsTab = new Admin_Model_DbTable_Orders2Products();
    	$orders2productsTab->updateCountUp($id, $product_id);
    	
    	$this->createPDF($id);
    	
    	if ($back==1){
    		$this->_helper->redirector->gotoRoute(
    			array(
    				'id'	=>	$id,
    				'back'	=>	$back
    			),
    			'admin_orders_orders-edit'
    		);
    	}
    }
	public function removeProductAction()
    {
    	$id = $this->_getParam('id');
    	$product_id = $this->_getParam('product_id');
    	$back = $this->_getParam('back');
    	
    	$orders2productsTab = new Admin_Model_DbTable_Orders2Products();
    	$orders2productsTab->updateCountDown($id, $product_id);
    	
    	$this->createPDF($id);
    	
    	if ($back==1){
    		$this->_helper->redirector->gotoRoute(
    			array(
    				'id'	=>	$id,
    				'back'	=>	$back
    			),
    			'admin_orders_orders-edit'
    		);
    	}
    }
	public function delProductAction()
    {
    	$id = $this->_getParam('id');
    	$product_id = $this->_getParam('product_id');
    	$back = $this->_getParam('back');
    	
    	$orders2productsTab = new Admin_Model_DbTable_Orders2Products();
    	$orders2productsTab->delProduct($id, $product_id);
    	
    	$this->createPDF($id);
    	
    	if ($back==1){
    		$this->_helper->redirector->gotoRoute(
    			array(
    				'id'	=>	$id,
    				'back'	=>	$back
    			),
    			'admin_orders_orders-edit'
    		);
    	}
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
}