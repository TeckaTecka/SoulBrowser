<?php
class Zend_Controller_Action_Helper_Cart extends Zend_Controller_Action_Helper_Abstract
{
	protected $front;
	protected $view;
	
	public function __construct() {
        $this->front = Zend_Controller_Front::getInstance();
        $viewRenderer = Zend_Controller_Action_HelperBroker::getStaticHelper('viewRenderer');
		$this->view = $viewRenderer->view;
    }
	
	public function Cart()
    {
    	Zend_Session::start();
    	if (Zend_Session::namespaceIsset('Cart')){
        	$cart = Zend_Session::namespaceGet('Cart');
        	if (isset($cart['products'])){
	        	$price = 0;
	        	$count = 0;
		    	foreach ($cart['products'] as $item) {
		    		$price += $item['price'] * $item['count'];
		    		$count += $item['count'];
		    	}
		    	$cart['products'] =  count($cart['products']);
		    	$cart['count'] = $count;
		    	$cart['totalPrice'] = number_format($price, 0, '.', ' ');
		    	$cart['currency'] = $this->getCurrency();
		    	$this->view->cart = $cart;
        	}
        }
        
       /* $this->view->headLink()
        	->prependStylesheet('/css/modules/cart/style.css');
        
        $this->view->jQuery()->addOnLoad(
        	'jQuery("#cart-bar").click(function(){
				jQuery("#cart-box").slideToggleWidth();
    		 });
    		jQuery("#cart-box").css("display","block");');*/
    }
	/**
     * Return currency
     * @return	string
     */
    private function getCurrency()
    {
    	$settingsTab = new Cart_Model_DbTable_Settings();
        $currency = $settingsTab->getFlag('eshop');
        return $currency['currency'];
    }
}