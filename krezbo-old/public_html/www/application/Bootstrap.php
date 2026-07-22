<?php

class Bootstrap extends Zend_Application_Bootstrap_Bootstrap
{
	protected function _initTimezone()
	{
		date_default_timezone_set("Europe/Prague");
	}
	protected function _initView()
	{
		$view = new Zend_View();
		//ZendX_JQuery::enableView($view);
		$view->addHelperPath("ZendX/JQuery/View/Helper", "ZendX_JQuery_View_Helper");
		$viewRenderer = new Zend_Controller_Action_Helper_ViewRenderer();
		$viewRenderer->setView($view);
		Zend_Controller_Action_HelperBroker::addHelper($viewRenderer);
      
		$view->doctype()->setDoctype('HTML5');
		
		$view->headTitle('Krezbo - čerpadla všeho druhu')
			 ->setSeparator(' | ')
			 ->setIndent("\t");
		
		$view->headMeta()
			->setHttpEquiv('content-type', 'text/html; charset=utf-8')
			//->setHttpEquiv('cache-control', 'no-store')
			//->setHttpEquiv('pragma', 'no-cache')
			->setIndent("\t");
						 
		$view->headMeta()
			->setName('author', 'rogr: www.rogr.cz')
			->setName('copyright', 'rogr: www.rogr.cz')
			->setName('description', 'Nabízíme prodej: čerpadel, domácích vodáren, ručních pump, zahradních hadic, náhradních dílů k čerpadlum i vodárnám v rámci maloobchodu i velkoobchodu. Dále nabízíme: teplovodní techniku včetně regulace a spojovacího materiálu (fitinky), montáže domácích vodáren. Součástí naší nabídky je i poradenská služba a servis.')
			->setName('keywords', 'čerpadla,domácí vodárny,ruční pumpy,zahradní hadice,náhradní díly k čerpadlům,teplovodní technika,fitinky,montáže domácích vodáren,poradenská služba,servis')
			->setName('robots', 'index,follow')
			->setName('googlebot', 'index,follow')
			->setName('resource-type', 'document')
			->setIndent("\t");

		$view->headLink(array('rel'	=>	'shortcut icon', 'href'	=>	'/ico/krezbo.ico', 'type'	=>	'image/x-icon'), 'append')
			->setIndent("\n\t");

		$view->jQuery()
			->setLocalPath('/js/jquery/jquery.min.js');
					   
		$view->headScript()
			->appendFile('/js/html5.js', 'text/javascript', array('conditional'	=>	'lt IE 9'))
			->setIndent("\t");
		
		$view->headLink()
			->appendStylesheet('/css/ie/ie7.css', 'screen', 'IE 7')
			->appendStylesheet('/css/ie/ie8.css', 'screen', 'IE 8')
			->prependStylesheet('/css/layout.css')
			->setIndent("\t");
    }
	protected function _initDB()
    {
        $config = new Zend_Config_Ini(APPLICATION_PATH . '/configs/application.ini', APPLICATION_ENV);
        
        $db = Zend_Db::factory($config->resources->db->adapter, $config->resources->db->params->toArray());
        try {
        	$db->query("SET NAMES 'utf8'");
        } catch (Exception $e) {
        	echo 'Databaze nenalezena';
        	exit;
        }
        Zend_Db_Table_Abstract::setDefaultAdapter($db);
        Zend_Registry::set('db', $db);
    }
}