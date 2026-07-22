<?php
class Zend_View_Helper_FullUrl extends Zend_View_Helper_Abstract {

    public function fullUrl() {
        $request = Zend_Controller_Front::getInstance()->getRequest();
        $url = /*$request->getScheme() . '://' . $request->getHttpHost() .*/ $this->view->url();
        return $url;
    }

}