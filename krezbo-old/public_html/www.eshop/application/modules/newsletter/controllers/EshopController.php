<?php

class Newsletter_EshopController extends Zend_Controller_Action
{
	public function init()
    {
    	$this->_helper->layout()->setLayout('eshop');
    	$this->_helper->eshop->initLayout(); 
    }
	public function addedAction()
    {
    	
    }
}