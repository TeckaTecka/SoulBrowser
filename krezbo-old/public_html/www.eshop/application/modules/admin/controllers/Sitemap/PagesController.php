<?php

class Admin_Sitemap_PagesController extends Zend_Controller_Action
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
    	$pagesTab = new Admin_Model_DbTable_Pages();
    	$pagesAll = $pagesTab->getPagesAll(1);
    	$this->view->pages = $pagesAll;
    	
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/table.css');
    	$this->view->jQuery()->addOnLoad(
    		'$("td span a[title], td span[title]").tooltip({
    			effect: "fade",
    			position: "top center",
    			opacity: 0.8,
    			predelay: 1000
    		});');
    }
    public function addAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css')
        	->prependStylesheet('/css/cleditor/jquery.cleditor.css');
        $this->view->headScript()->appendFile('/js/jquery.tools.min.js')
								 ->appendFile('/js/cleditor/jquery.cleditor.min.js')
								 ->appendFile('/js/cleditor/jquery.cleditor.table.min.js')
								 ->appendFile('/js/cleditor/jquery.cleditor.icon.min.js');
        $this->view->jQuery()->addOnLoad(
        	'$("#form-add :input.tool-tip").tooltip({
        		effect: "fade",
        		position: "center right",
        		offset: [0, 20],
        		opacity: 0.8
    		 });
    		 $("#page").cleditor({
    		 	width:"478",
    		 	height:"300"
    		 });
		');
        
    	$formPages = new Admin_Form_Sitemap_Pages_Pages();
    	$formPages->setAction($this->view->url(array(), 'admin_sitemap_pages_add'));
    	$formPages->setAttrib('id', 'form-add');
    	$this->view->formPages = $formPages;
    	
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    	if ($formPages->isValid($formData))
	    	{
	    		$pagesTab = new Admin_Model_DbTable_Pages();
	    		$id = $pagesTab->setPage(
	    			$formData['title'],
		    		$formData['title_menu'],
		    		$formData['title_url'],
		    		$formData['page'],
		    		0);
	    		
		    	$this->_helper->redirector->gotoRoute(array('id'	=>	$id), 'admin_sitemap_pages_edit');
	    	}
    	}
    }
	public function editAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css')
        	->prependStylesheet('/css/cleditor/jquery.cleditor.css');
        $this->view->headScript()->appendFile('/js/jquery.tools.min.js')
        						 ->appendFile('/js/cleditor/jquery.cleditor.min.js')
        						 ->appendFile('/js/cleditor/jquery.cleditor.table.min.js')
        						 ->appendFile('/js/cleditor/jquery.cleditor.icon.min.js');
        $this->view->jQuery()->addOnLoad(
        	'$("#form-edit :input.tool-tip").tooltip({
        		effect: "fade",
        		position: "center right",
        		offset: [0, 20],
        		opacity: 0.8
    		 });
    		 $("#page").cleditor({
    		 	width:"478",
    		 	height:"300"
    		 });
    		 $("#pages").click(function(){
    		 	$("#tab1").css({display: "block"});$("#pages").addClass("active");
    			$("#tab2").css({display: "none"});$("#settings").removeClass("active");
    		 });
    		 $("#settings").click(function(){
    			$("#tab1").css({display: "none"});$("#pages").removeClass("active");
    			$("#tab2").css({display: "block"});$("#settings").addClass("active");
    		 });
    		');
        
    	$id = $this->_getParam('id');// id clanku
    	
    	// FORM PAGES
    	$formPages = new Admin_Form_Sitemap_Pages_Pages();
    	$formPages->setAction($this->view->url(array(), 'admin_sitemap_pages_edit'));
    	$formPages->setAttrib('id', 'form-edit');
    	$this->view->formPages = $formPages;
    	
    	// FORM SETTINGS **************************************************************************
    	$formSettings = new Admin_Form_Sitemap_Pages_Settings();
    	$formSettings->setAction($this->view->url(array(), 'admin_sitemap_pages_edit'));
    	$formSettings->setAttrib('id', 'form-settings');
    	$this->view->formSettings = $formSettings;
    	
    	$pagesTab = new Admin_Model_DbTable_Pages();
		$page = $pagesTab->getPage($id);
		$data = array(
			'title'			=>	$page['title'],
			'title_menu'	=>	$page['title_menu'],
			'title_url'		=>	$page['title_url'],
			'page'			=>	$page['page'],
			'show'			=>	$page['show']);
    	$formPages->populate($data);
    	$formSettings->populate($data);
    	
    	/*****************************************************************************************/
    	
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    	
			// FORM PAGES *************************************************************************
	    	$savePage = $this->getRequest()->getPost('savePage');
	    	if ($savePage)
	    	{
	    		if ($formPages->isValid($formData))
	    		{
	    			$pagesTab->updatePage(
	    				$id,
		    			$formData['title'],
		    			$formData['title_menu'],
		    			$formData['title_url'],
		    			$formData['page'],
		    			$data['show']
		    		);
	    		}
	    	}
	    	// FORM SETTINGS **********************************************************************
	    	$saveSettings = $this->getRequest()->getPost('saveSettings');
	    	if ($saveSettings)
	    	{
	    		$pagesTab->updatePage(
	    			$id,
		    		$data['title'],
		    		$data['title_menu'],
		    		$data['title_url'],
		    		$data['page'],
		    		$formData['show']
		    	);
	    	}
		}
	}
	public function delAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css');
    		
    	$id = $this->_getParam('id');// id clanku
    	
    	$form = new Admin_Form_Sitemap_Pages_Del();
    	$form->setAction($this->view->url(array('id' => $id), 'admin_sitemap_pages_del'));
    	$this->view->form = $form;
    	
    	$pagesTab = new Admin_Model_DbTable_Pages();
    	$page = $pagesTab->getPage($id);
    	$this->view->page = $page['title'];
    	
    	if ($this->getRequest()->isPost()) {
            $delete = $this->getRequest()->getPost('delete');
                        
            if ($delete != NULL) {
            	
            	$pagesTab->delPage($id);
          	}
            $this->_helper->redirector->gotoRoute(array(), 'admin_sitemap_pages_index');
    	}
    }
    public function homePageAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css')
    		->prependStylesheet('/css/cleditor/jquery.cleditor.css');
        $this->view->headScript()->appendFile('/js/jquery.tools.min.js')
        						 ->appendFile('/js/cleditor/jquery.cleditor.min.js')
        						 ->appendFile('/js/cleditor/jquery.cleditor.table.min.js')
        						 ->appendFile('/js/cleditor/jquery.cleditor.icon.min.js');
        $this->view->jQuery()->addOnLoad(
        	'$("#form-edit :input.tool-tip").tooltip({
        		effect: "fade",
        		position: "center right",
        		offset: [0, 20],
        		opacity: 0.8
    		 });
    		 $("#page").cleditor({
    		 	width:"478",
    		 	height:"300"
    		 });
    		 $("#pages").click(function(){
    		 	$("#tab1").css({display: "block"});$("#pages").addClass("active");
    			$("#tab2").css({display: "none"});$("#settings").removeClass("active");
    		 });
    		 $("#settings").click(function(){
    			$("#tab1").css({display: "none"});$("#pages").removeClass("active");
    			$("#tab2").css({display: "block"});$("#settings").addClass("active");
    		 });
    		');
        
    	$id = 1;// id clanku
    	
    	// FORM PAGES
    	$formPages = new Admin_Form_Sitemap_Pages_HomePage();
    	$formPages->setAction($this->view->url(array(), 'admin_sitemap_pages_home-page'));
    	$formPages->setAttrib('id', 'form-edit');
    	$this->view->formPages = $formPages;
    	
    	// FORM SETTINGS **************************************************************************
    	$formSettings = new Admin_Form_Sitemap_Pages_Settings();
    	$formSettings->setAction($this->view->url(array(), 'admin_sitemap_pages_home-page'));
    	$formSettings->setAttrib('id', 'form-settings');
    	$this->view->formSettings = $formSettings;
    	
    	$pagesTab = new Admin_Model_DbTable_Pages();
		$page = $pagesTab->getPage($id);
		$data = array(
			'title'			=>	$page['title'],
			'title_menu'	=>	$page['title_menu'],
			'title_url'		=>	$page['title_url'],
			'page'			=>	$page['page'],
			'show'			=>	$page['show']);
    	$formPages->populate($data);
    	$formSettings->populate($data);
    	
    	/*****************************************************************************************/
    	
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    	
			// FORM PAGES *************************************************************************
	    	$savePage = $this->getRequest()->getPost('savePage');
	    	if ($savePage)
	    	{
	    		if ($formPages->isValid($formData))
	    		{
	    			$pagesTab->updatePage(
	    				$id,
		    			$formData['title'],
		    			'Titulní stránka',
						'titulni-stranka',
		    			$formData['page'],
		    			$data['show']
		    		);
	    		}
	    	}
	    	// FORM SETTINGS **********************************************************************
	    	$saveSettings = $this->getRequest()->getPost('saveSettings');
	    	if ($saveSettings)
	    	{
	    		$pagesTab->updatePage(
	    			$id,
		    		$data['title'],
		    		$data['title_menu'],
		    		$data['title_url'],
		    		$data['page'],
		    		$formData['show']
		    	);
	    	}
		}
    }
}