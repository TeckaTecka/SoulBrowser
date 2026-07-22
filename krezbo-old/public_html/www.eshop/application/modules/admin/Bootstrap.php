<?php

class Admin_Bootstrap extends Zend_Application_Module_Bootstrap
{
	function _initSetRoutes()
	{
		$config = new Zend_Config_Ini(APPLICATION_PATH.'/modules/admin/configs/routes.ini', 'production');
		
		$front = Zend_Controller_Front::getInstance();
		
		$router = $front->getRouter();
		$router->addConfig($config, 'routes');
	}
	protected function _initHelpers()
	{
		Zend_Controller_Action_HelperBroker::addPath(APPLICATION_PATH .'/modules/admin/controllers/helpers');
	}
}