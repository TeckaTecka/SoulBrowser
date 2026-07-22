<?php

class Admin_Products_WatermarkController extends Zend_Controller_Action
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
    		->prependStylesheet('/css/admin/product.css')
    		->prependStylesheet('/css/shared/form.css');
    	$this->view->jQuery()->addOnLoad(
    		'$("#watermark").click(function(){
    		 	$("#tab1").css({display: "block"});$("#watermark").addClass("active");
    			$("#tab2").css({display: "none"});$("#load").removeClass("active");
    			$("#tab3").css({display: "none"});$("#settings").removeClass("active");
    		 });
    		 $("#load").click(function(){
    		 	$("#tab1").css({display: "none"});$("#watermark").removeClass("active");
    			$("#tab2").css({display: "block"});$("#load").addClass("active");
    			$("#tab3").css({display: "none"});$("#settings").removeClass("active");
    		 });
    		 $("#settings").click(function(){
    		 	$("#tab1").css({display: "none"});$("#watermark").removeClass("active");
    			$("#tab2").css({display: "none"});$("#load").removeClass("active");
    			$("#tab3").css({display: "block"});$("#settings").addClass("active");
    		 });
    		 ');
    	// FORM WATERMARK *************************************************************************
        $formWatermark = new Admin_Form_Products_Watermark_Watermark();
    	$formWatermark->setAction($this->view->url(array(), 'admin_watermark_index'));
    	$formWatermark->setAttrib('id', 'form-edit-watermark');
    	$this->view->formWatermark = $formWatermark;
    	// ****************************************************************************************
    	// FORM SETTINGS **************************************************************************
        $formSettings = new Admin_Form_Products_Watermark_Settings();
    	$formSettings->setAction($this->view->url(array(), 'admin_watermark_index'));
    	$formSettings->setAttrib('id', 'form-edit-settings');
    	$this->view->formSettings = $formSettings;
    	
    	$settingsTab = new Admin_Model_DbTable_Settings();
    	$watermarkSettings = $settingsTab->getFlag('products');
    	$watermarkSettings = $watermarkSettings['watermark'];
    	if ($watermarkSettings){
    		$formSettings->enable->setValue(1);
    	}
    	// ****************************************************************************************
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	// FORM WATERMARK *********************************************************************
	    	$saveWatermark = $this->getRequest()->getPost('saveWatermark');
	    	if ($saveWatermark)
	    	{
	    		$this->view->jQuery()->addOnLoad('$("#load").click();');
	    		if ($formWatermark->isValid($formData))
	    		{
	    			//Image
					$adapter = $formWatermark->watermark->getTransferAdapter();
					//Zend_Debug::dump($adapter);
	    			$infoImage = $adapter->getFileInfo('watermark');
	    			//Zend_Debug::dump($infoImage, $label='$infoImage: ', $echo=true);
	    			$fileName = 'watermark';
	    			$suffix = '.png';
	    			//Zend_Debug::dump($fileName);
    				$adapter->addFilter('Rename',
					    						array('target'=>'data/png/watermark/'.$fileName.$suffix,
					    						'overwrite'=>true));
					
					$adapter->receive($infoImage['watermark']['name']);
				}
	    	}
	    	// ************************************************************************************
	    	// FORM SETTINGS **********************************************************************
	    	$saveSettings = $this->getRequest()->getPost('saveSettings');
	    	if ($saveSettings)
	    	{
	    		$this->view->jQuery()->addOnLoad('$("#settings").click();');
	    		if ($formSettings->isValid($formData))
	    		{
	    			$settingsTab->updateFlag(
	    				'products',
	    				'watermark',
	    				$formData['enable']
	    			);
				}
	    	}
	    	// ************************************************************************************
    	}
    }
    
}