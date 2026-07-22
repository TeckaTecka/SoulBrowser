<?php

class Admin_Products_ParametersController extends Zend_Controller_Action
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
    		->prependStylesheet('/css/shared/table.css')
    		->prependStylesheet('/css/shared/paginator.css');
    	$this->view->jQuery()->addOnLoad(
    		'$("td span[title], td span a[title]").tooltip({
    			effect: "fade",
    			position: "top center",
    			opacity: 0.8,
    			predelay: 1000
    		 });
    		 $("#parameters").click(function(){
    		 	$("#tab1").css({display: "block"});$("#parameters").addClass("active");
    			$("#tab2").css({display: "none"});$("#parameters_groups").removeClass("active");
    		 });
    		 $("#parameters_groups").click(function(){
    			$("#tab1").css({display: "none"});$("#parameters").removeClass("active");
    			$("#tab2").css({display: "block"});$("#parameters_groups").addClass("active");
    		 });');
    	
    	$page = $this->_getParam('page');
    	
    	$parametersTab = new Admin_Model_DbTable_Parameters();
    	$parameters = $parametersTab->getParameters($page);
    	$this->view->parameters = $parameters;
    	//Zend_Debug::dump($parameters);
    	
    	$db = Zend_Registry::get('db');
    	$adapter = new Zend_Paginator_Adapter_DbSelect(
    		$db->select()
    			->from('parameters')
    			->where('flags IS NULL')
    	);
    	$paginator = new Zend_Paginator($adapter);
    	$paginator->setItemCountPerPage(20)
    		->setCurrentPageNumber($page);
    	//Zend_Debug::dump($paginator);
    	$this->view->paginator = $paginator;
    	
    	// PARAMETERS GROUPS **********************************************************************
    	$tab = $this->_getParam('tab');
    	if($tab == 2){
    		$this->view->jQuery()->addOnLoad('$("#parameters_groups").click();');
    	}
    	
    	$parametersGroupsTab = new Admin_Model_DbTable_ParametersGroups();
    	$parametersGroups = $parametersGroupsTab->getGroups();
    	$this->view->parametersGroups = $parametersGroups;
    }
    public function addAction()
    {
    	$this->view->headLink()
        	->prependStylesheet('/css/shared/form.css');
        $this->view->headScript()
        	->appendFile('/js/jquery.tools.min.js');
        $this->view->jQuery()->addOnLoad(
        	'$("form :input.tool-tip").tooltip({
        		effect: "fade",
        		offset: [20, 100],
        		opacity: 0.8
    		 });');
        
    	// FORM PARAMETER *************************************************************************
        $formParameter = new Admin_Form_Products_Parameters_Parameter();
    	$formParameter->setAction($this->view->url(array(), 'admin_products_parameters-add'));
    	$formParameter->setAttrib('id', 'form-add-parameter');
    	$this->view->formParameter = $formParameter;
    	
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    		
	    	if ($formParameter->isValid($formData))
	    	{
    			$parametersTab = new Admin_Model_DbTable_Parameters();
    			$id = $parametersTab->setParameter($formData['title']);
    			
	    		$this->_helper->redirector->gotoRoute(array(), 'admin_products_parameters-index');
	    	}
        }
    }
	public function editAction()
    {
    	$this->view->headLink()
        	->prependStylesheet('/css/shared/form.css');
        $this->view->headScript()
        	->appendFile('/js/jquery.tools.min.js');
        $this->view->jQuery()->addOnLoad(
        	'$("form :input.tool-tip").tooltip({
        		effect: "fade",
        		offset: [20, 100],
        		opacity: 0.8
    		 });');
        
    	$id = $this->_getParam('id');
    	
    	// FORM PARAMETER *************************************************************************
        $formParameter = new Admin_Form_Products_Parameters_Parameter();
    	$formParameter->setAction($this->view->url(array(), 'admin_products_parameters-edit'));
    	$formParameter->setAttrib('id', 'form-edit-parameter');
    	$this->view->formParameter = $formParameter;
    	/*****************************************************************************************/
		$parametersTab = new Admin_Model_DbTable_Parameters();
		
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    	
			// FORM PARAMETER ***********************************************************************
	    	$saveParameter = $this->getRequest()->getPost('saveParameter');
	    	if ($saveParameter)
	    	{
	    		if ($formParameter->isValid($formData))
	    		{
	    			$parametersTab->updateParameter(
	    				$id,
	    				$formData['title']
			    	);
	    		}
	    		
	    		$this->_helper->redirector->gotoRoute(array(), 'admin_products_parameters-index');
	    	}
    	}else{
	    	$parameter = $parametersTab->getParameter($id);
	    	$data = array(
	    		'title'	=>	$parameter['title']
	    	);
    		$formParameter->populate($data);
	    	}
	}
	public function delAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css');
    	
    	$id = $this->_getParam('id');
    	
    	$form = new Admin_Form_Products_Parameters_Del();
    	$form->setAction($this->view->url(array('id' => $id), 'admin_products_parameters-del'));
    	$this->view->form = $form;
    	
    	$parametersTab = new Admin_Model_DbTable_Parameters();
    	
    	$parameter = $parametersTab->getParameter($id);
    	$this->view->parameter = $parameter['title'];
    	
    	if ($this->getRequest()->isPost()) {
            $delete = $this->getRequest()->getPost('delete');
                        
            if ($delete != NULL) {
            	$parametersTab->setFlag($id, 'delete');
          	}
            $this->_helper->redirector->gotoRoute(array(), 'admin_products_parameters-index');
    	}
    }
    public function addGroupAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css');
    	
    	// FORM GROUP *****************************************************************************
        $formGroup = new Admin_Form_Products_Parameters_Group();
    	$formGroup->setAction($this->view->url(array(), 'admin_products_parameters_add-group'));
    	$formGroup->setAttrib('id', 'form-add-parameters-group');
    	$this->view->formGroup = $formGroup;
    	
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    		
	    	if ($formGroup->isValid($formData))
	    	{
    			$parametersGroupsTab = new Admin_Model_DbTable_ParametersGroups();
    			$id = $parametersGroupsTab->setGroup($formData['title']);
    			
	    		$this->_helper->redirector->gotoRoute(
	    			array(
	    				'id'	=>	$id,
	    				'tab'	=>	2
	    			),
	    			'admin_products_parameters_edit-group'
	    		);
	    	}
        }
    }
    public function editGroupAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css')
    		->prependStylesheet('/css/shared/table.css');
    	$this->view->jQuery()->addOnLoad(
    		'$("td span[title], td span a[title]").tooltip({
    			effect: "fade",
    			position: "top center",
    			opacity: 0.8,
    			predelay: 1000
    		 });
    		 $("#parameter").click(function(){
    		 	$("#tab1").css({display: "block"});$("#parameter").addClass("active");
    			$("#tab2").css({display: "none"});$("#parameter-edit").removeClass("active");
    		 });
    		 $("#parameter-edit").click(function(){
    			$("#tab1").css({display: "none"});$("#parameter").removeClass("active");
    			$("#tab2").css({display: "block"});$("#parameter-edit").addClass("active");
    		 });');
    	
    	$tab = $this->_getParam('tab');
    	if($tab == 2){
    		$this->view->jQuery()->addOnLoad('$("#parameter-edit").click();');
    	}
    	
    	$id = $this->_getParam('id');
    	$this->view->group_id = $id;
    	// FORM GROUP *****************************************************************************
        $formGroup = new Admin_Form_Products_Parameters_Group();
    	$formGroup->setAction($this->view->url(
    		array(
    			'id'	=>	$id,
	    		'tab'	=>	1
    		),
    		'admin_products_parameters_edit-group'
    	));
    	$formGroup->setAttrib('id', 'form-edit-parameters-group');
    	$this->view->formGroup = $formGroup;
    	
    	$parametersGroupsTab = new Admin_Model_DbTable_ParametersGroups();
    	$group = $parametersGroupsTab->getGroup($id);
    	$formGroup->title->setValue($group['title']);
    	// ****************************************************************************************
    	// FORM GROUP PARAMETER *******************************************************************
        $formGroupParameter = new Admin_Form_Products_Parameters_GroupParameter();
    	$formGroupParameter->setAction($this->view->url(
    		array(
    			'id'	=>	$id,
	    		'tab'	=>	2
    		),
    		'admin_products_parameters_edit-group'
    	));
    	$formGroupParameter->setAttrib('id', 'form-add-parameter-to-group');
    	$this->view->formGroupParameter = $formGroupParameter;
    	$parametersTab = new Admin_Model_DbTable_Parameters();
    	$parametersPairs = $parametersTab->getParametersPairs();
    	$formGroupParameter->parameters->setMultiOptions($parametersPairs);
    	
    	// ****************************************************************************************
    	
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    	
			// FORM GROUP *************************************************************************
	    	$saveGroup = $this->getRequest()->getPost('saveGroup');
	    	if ($saveGroup)
	    	{
	    		if ($formGroup->isValid($formData))
	    		{
	    			$parametersGroupsTab->updateGroup(
	    				$id,
	    				$formData['title']
			    	);
	    		}
	    	}
    		// FORM GROUP PARAMETER ***************************************************************
	    	$saveParameter = $this->getRequest()->getPost('saveParameter');
	    	if ($saveParameter)
	    	{
	    		if ($formGroupParameter->isValid($formData))
	    		{
	    			$parametersGroupsTab->setGroups2parameters(
	    				$id,
	    				$formData['parameters']
			    	);
	    		}
	    	}
    	}
    	
    	
    	$parameters = $parametersGroupsTab->getParameters($id);
    	$this->view->parameters = $parameters;
    	//Zend_Debug::dump($parameters);
    }
    public function editGroupDelParamAction()
    {
    	$id = $this->_getParam('id');
    	$tab = $this->_getParam('tab');
    	$pg2p_id = $this->_getParam('pg2p_id');
    	
    	$parametersGroupsTab = new Admin_Model_DbTable_ParametersGroups();
    	$parametersGroupsTab->delGroups2parameters($pg2p_id);
    	
    	$this->_helper->redirector->gotoRoute(
	    			array(
	    				'id'	=>	$id,
	    				'tab'	=>	2
	    			),
	    			'admin_products_parameters_edit-group'
	    		);
    }
    public function delGroupAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css');
    	
    	$id = $this->_getParam('id');
    	$tab = $this->_getParam('tab');
    	
    	$form = new Admin_Form_Products_Parameters_Del();
    	$form->setAction($this->view->url(
    		array(
    			'id'	=>	$id,
    			'tab'	=>	2
    		),
    		'admin_products_parameters_del-group'
    	));
    	$this->view->form = $form;
    	
    	$parametersGroupsTab = new Admin_Model_DbTable_ParametersGroups();
    	$group = $parametersGroupsTab->getGroup($id);
    	$this->view->group = $group['title'];
    	
    	if ($this->getRequest()->isPost()) {
            $delete = $this->getRequest()->getPost('delete');
                        
            if ($delete != NULL) {
            	$parametersGroupsTab->delGroup($id);
          	}
            $this->_helper->redirector->gotoRoute(
            	array(
            		'tab'	=>	$tab
            	),
            	'admin_products_parameters-index'
            );
    	}
    }
}