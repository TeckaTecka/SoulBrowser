<?php

class Admin_Sitemap_CategoriesController extends Zend_Controller_Action
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
    	$categoriesTab = new Admin_Model_DbTable_Categories();
    	$categoriesAll = $categoriesTab->getCategoriesAll();
    	$this->view->catTree = $categoriesAll;
    	
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/table.css');
    		//->prependStylesheet('/css/admin/table.treeTable.css');
    	//$this->view->headScript()->appendFile('/js/jquery.treeTable.min.js');    	
    	$this->view->jQuery()->addOnLoad(
    		//'$("#tree_table").treeTable();
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
        
        $formCategories = new Admin_Form_Sitemap_Categories_Categories();
    	$formCategories->setAction($this->view->url(array(), 'admin_sitemap_categories-add'));
    	$formCategories->setAttrib('id', 'form-add');
    	
    	$categoriesTab = new Admin_Model_DbTable_Categories();
    	//$categoriesTreeTab = new Admin_Model_DbTable_CategoriesTree();
    	
    	/*$data = $categoriesTab->getCategoriesAll();
    	if ($data){
	    	for ($i = 0; $i < count($data); $i++) {
	    		$countOfParents = $categoriesTreeTab->getCountOfParents($data[$i]['sub']);
	    		$options[$data[$i]['id']] = str_repeat('->', $countOfParents).' '.$data[$i]['title'];
	    	}
	    	$formCategories->sort_by->setMultiOptions($options);
    	}
    	$formCategories->order
    		->setRequired(false)
    		->setAttrib('disabled', true)
    		->setDescription('Pořadí se vyplní automaticky');
    	*/	
    	$this->view->formCategories = $formCategories;
    	
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    	if ($formCategories->isValid($formData))
	    	{
	    		if ($categoriesTab->existUrlCategory(($formData['title_url'])?$formData['title_url']:$categoriesTab->Convert($formData['title']))){
	    			$formCategories->title_url
	    				->setDescription('Tato URL adresa již existuje')
	    				->addDecorator('Description', array('class'	=>	'errors'));
	    			if ($formData['title_url'] == ''){
		    			$data = array(
							'title_url'	=>	$categoriesTab->Convert($formData['title'])
		    			);
				    	$formCategories->populate($data);
	    			}
	    		}else{
		    		$id = $categoriesTab->setCategory(
		    			$formData['title'],
			    		$formData['title_menu'],
			    		$formData['title_url'],
			    		$formData['description']
			    	);
			    	
		    		//$subCategories = $categoriesTreeTab->getCategories($formData['sort_by']);
		    		
	    			/*$categoriesTreeTab->setCategory(
	    				$id,
	    				$formData['sort_by'],
	    				(count($subCategories)) + 1
	    			);*/
		    		
		    		$this->_helper->redirector->gotoRoute(array(), 'admin_sitemap_categories-saved');
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
    	
    	// FORM CATEGORIES ************************************************************************
    	$formCategories = new Admin_Form_Sitemap_Categories_Categories();
    	$formCategories->setAction($this->view->url(array('id'	=>	$id), 'admin_sitemap_categories-edit'));
    	$formCategories->setAttrib('id', 'form-add');
    	$this->view->formCategories = $formCategories;
    	
    	$categoriesTab = new Admin_Model_DbTable_Categories();
    	//$categoriesTreeTab = new Admin_Model_DbTable_CategoriesTree();
    	
    	$data = $categoriesTab->getCategoriesAll();
    	//Zend_Debug::dump($data);
    	/*for ($i = 0; $i < count($data); $i++) {
    		$isSubcategory = $categoriesTreeTab->isSubCategory($data[$i]['id'], $id);
    		//Zend_Debug::dump($isSubcategory);
    		if (!$isSubcategory){
	    		$countOfParents = $categoriesTreeTab->getCountOfParents($data[$i]['sub']);
	    		$options[$data[$i]['id']] = str_repeat('->', $countOfParents).' '.$data[$i]['title'];
    		}
    	}
    	$formCategories->sort_by->setMultiOptions($options);
    	*/
    	$category = $categoriesTab->getCategory($id);
    	
    	/*$subCategories = $categoriesTreeTab->getCategories($category['sub']);
    	$options = array();
    	for ($i = 1; $i <= 25; $i++) {
    		$orderExist = false;
    		foreach ($subCategories as $sub) {
    			if ($sub['order'] == $i){
    				$orderExist = true;
    			}
    		}
    		if (!$orderExist){
    			$options[$i] = $i;
    		}
    	}
    	$options[$category['order']] = $category['order'];
    	asort($options, SORT_NUMERIC);
    	$formCategories->order->setMultiOptions($options);
    	*/
    	
		$data = array(
			//'sort_by'		=>	$category['sub'],
			//'order'			=>	$category['order'],
			'title'			=>	$category['title'],
			'title_menu'	=>	$category['title_menu'],
			'title_url'		=>	$category['title_url'],
			'description'	=>	$category['description']);
    	$formCategories->populate($data);
    		
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	if ($formCategories->isValid($formData))
	    	{
	    		if (($formData['title_url'] == '') OR ($formData['title_menu'] == '')){
	    			$data = array(
	    				'title_menu'	=>	$formData['title'],
						'title_url'		=>	$categoriesTab->Convert($formData['title'])
	    			);
			    	$formCategories->populate($data);
			    	$formData['title_menu'] = $data['title_menu'];
			    	$formData['title_url'] = $data['title_url'];
    			}
	    		$exist = $categoriesTab->existUrlCategory($formData['title_url']);
	    		//Zend_Debug::dump($exist);
	    		//Zend_Debug::dump($category['title_url']);
	    		//Zend_Debug::dump($formData['title_url']);
	    		if (($exist > 1) OR (($exist == 1) AND ($category['title_url'] != $formData['title_url']))){
	    			$formCategories->title_url
	    				->setDescription('Tato URL adresa již existuje')
	    				->addDecorator('Description', array('class'	=>	'errors'));
	    			
	    		}else{
		    		$categoriesTab->updateCategory(
		    			$id,
			    		$formData['title'],
			    		$formData['title_menu'],
			    		$formData['title_url'],
			    		$formData['description']);
			    								   
			    	/*$categoriesTreeTab->updateCategory(
			    		$id,
			    		$formData['sort_by'],
			    		$formData['order']
			    	);*/
			    	
			    	$this->_helper->redirector->gotoRoute(array(), 'admin_sitemap_categories-saved');
	    		}
	    	}
        }
    }
	public function delAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css');
    		
    	$id = $this->_getParam('id');// id kategorie
    	
    	$form = new Admin_Form_Sitemap_Categories_Del();
    	$form->setAction($this->view->url(array('id' => $id), 'admin_sitemap_categories-del'));
    	$this->view->form = $form;
    	
    	$categoriesTab = new Admin_Model_DbTable_Categories();
    	$category = $categoriesTab->getCategory($id);
    	$this->view->category = $category['title'];
    	
    	if ($this->getRequest()->isPost()) {
            $delete = $this->getRequest()->getPost('delete');
                        
            if ($delete != NULL) {
            	$categoriesTab->delCategory($id);
            }
            $this->_helper->redirector->gotoRoute(array(), 'admin_sitemap_categories-index');
    	}
    }
    public function savedAction()
    {
    	
    }
}