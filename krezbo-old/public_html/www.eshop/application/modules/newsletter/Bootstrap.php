<?php

class Newsletter_Bootstrap extends Zend_Application_Module_Bootstrap
{
	function _initSetRoutes()
	{
		$config = new Zend_Config_Ini(APPLICATION_PATH.'/modules/newsletter/configs/routes.ini', 'production');
		
		$front = Zend_Controller_Front::getInstance();
		
		$router = $front->getRouter();
		$router->addConfig($config, 'routes');
	}
	protected function _initHelpers()
	{
		Zend_Controller_Action_HelperBroker::addPath(APPLICATION_PATH .'/modules/newsletter/controllers/helpers');
	}
	protected function _initScripts()
	{
		$viewRenderer = Zend_Controller_Action_HelperBroker::getStaticHelper('viewRenderer');
		$view = $viewRenderer->view;
		$view->addScriptPath(APPLICATION_PATH .'/modules/newsletter/views/scripts');
	}
}