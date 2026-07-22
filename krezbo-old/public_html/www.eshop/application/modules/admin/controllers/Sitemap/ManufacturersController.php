<?php

class Admin_Sitemap_ManufacturersController extends Zend_Controller_Action
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
    	$manufacturersTab = new Admin_Model_DbTable_Manufacturers();
    	$manufacturers = $manufacturersTab->getManufacturers();
    	$this->view->manufacturers = $manufacturers;
    	
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/table.css'); 	
    	$this->view->jQuery()->addOnLoad(
    		 '$("span a[title]").tooltip({
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
        $this->view->headScript()
        	->appendFile('/js/jquery.tools.min.js')
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
    		 $("#description").cleditor({
    		 	width:"478",
    		 	height:"300"
    		 });
    	');
        
        $formManufacturers = new Admin_Form_Sitemap_Manufacturers_Manufacturers();
    	$formManufacturers->setAction($this->view->url(array(), 'admin_sitemap_manufacturers-add'));
    	$formManufacturers->setAttrib('id', 'form-add');
    	$this->view->formManufacturers = $formManufacturers;
    	
    	$manufacturersTab = new Admin_Model_DbTable_Manufacturers();
    	
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    	if ($formManufacturers->isValid($formData))
	    	{
	    		if ($manufacturersTab->existUrl(($formData['title_url'])?$formData['title_url']:$manufacturersTab->Convert($formData['title']))){
	    			$formManufacturers->title_url
	    				->setDescription('Tato URL adresa již existuje')
	    				->addDecorator('Description', array('class'	=>	'errors'));
	    			if ($formData['title_url'] == ''){
		    			$data = array(
							'title_url'	=>	$manufacturersTab->Convert($formData['title'])
		    			);
				    	$formManufacturers->populate($data);
	    			}
	    		}else{
		    		$id = $manufacturersTab->setManufactury(
		    			$formData['title'],
			    		$formData['title_menu'],
			    		$formData['title_url'],
			    		$formData['description']
			    	);
			    	
		    		$this->_helper->redirector->gotoRoute(array(), 'admin_sitemap_manufacturers-index');
	    		}
	    	}
    	}        
    }
	public function editAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css')
        	->prependStylesheet('/css/cleditor/jquery.cleditor.css');
        $this->view->headScript()
        	->appendFile('/js/jquery.tools.min.js')
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
    		 $("#description").cleditor({
    		 	width:"478",
    		 	height:"300"
    		 });
    	');
        
    	$id = $this->_getParam('id');
    	
    	// FORM MANUFACTURERS *********************************************************************
    	$formManufacturers = new Admin_Form_Sitemap_Manufacturers_Manufacturers();
    	$formManufacturers->setAction($this->view->url(array('id'	=>	$id), 'admin_sitemap_manufacturers-edit'));
    	$formManufacturers->setAttrib('id', 'form-add');
    	$this->view->formManufacturers = $formManufacturers;
    	
    	$manufacturersTab = new Admin_Model_DbTable_Manufacturers();
    	
    	$manufactury = $manufacturersTab->getManufactury($id);
    	$data = array(
			'title'			=>	$manufactury['title'],
			'title_menu'	=>	$manufactury['title_menu'],
			'title_url'		=>	$manufactury['title_url'],
			'description'	=>	$manufactury['description']
    	);
    	$formManufacturers->populate($data);
    		
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	if ($formManufacturers->isValid($formData))
	    	{
	    		if (($formData['title_url'] == '') OR ($formData['title_menu'] == '')){
	    			$data = array(
	    				'title_menu'	=>	$formData['title'],
						'title_url'		=>	$manufacturersTab->Convert($formData['title'])
	    			);
			    	$formManufacturers->populate($data);
			    	$formData['title_menu'] = $data['title_menu'];
			    	$formData['title_url'] = $data['title_url'];
    			}
	    		$exist = $manufacturersTab->existUrl($formData['title_url']);
	    		//Zend_Debug::dump($exist);
	    		//Zend_Debug::dump($category['title_url']);
	    		//Zend_Debug::dump($formData['title_url']);
	    		if (($exist > 1) OR (($exist == 1) AND ($manufactury['title_url'] != $formData['title_url']))){
	    			$formCategories->title_url
	    				->setDescription('Tato URL adresa již existuje')
	    				->addDecorator('Description', array('class'	=>	'errors'));
	    			
	    		}else{
		    		$manufacturersTab->updateManufactury(
		    			$id,
			    		$formData['title'],
			    		$formData['title_menu'],
			    		$formData['title_url'],
			    		$formData['description']);
			    								   
			    	$this->_helper->redirector->gotoRoute(array(), 'admin_sitemap_manufacturers-index');
	    		}
	    	}
        }
    }
	public function delAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css');
    		
    	$id = $this->_getParam('id');// id kategorie
    	
    	$form = new Admin_Form_Sitemap_Manufacturers_Del();
    	$form->setAction($this->view->url(array('id' => $id), 'admin_sitemap_manufacturers-del'));
    	$this->view->form = $form;
    	
    	$manufacturersTab = new Admin_Model_DbTable_Manufacturers();
    	$manufactury = $manufacturersTab->getManufactury($id);
    	$this->view->manufactury = $manufactury['title'];
    	
    	if ($this->getRequest()->isPost()) {
            $delete = $this->getRequest()->getPost('delete');
                        
            if ($delete != NULL) {
            	$manufacturersTab->delManufactory($id);
            }
            $this->_helper->redirector->gotoRoute(array(), 'admin_sitemap_manufacturers-index');
    	}
    }
    public function savedAction()
    {
    	
    }
}