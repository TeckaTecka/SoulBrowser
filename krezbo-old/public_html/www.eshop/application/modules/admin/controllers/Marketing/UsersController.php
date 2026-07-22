<?php

class Admin_Marketing_UsersController extends Zend_Controller_Action
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
    		'$("td span a[title]").tooltip({
    			effect: "fade",
    			position: "top center",
    			opacity: 0.8,
    			predelay: 500
    		 });');
    	
    	$page = $this->_getParam('page');
    	
    	$usersTab = new Admin_Model_DbTable_Users();
    	$users = $usersTab->getUsers($page);
    	$this->view->users = $users;
    	//Zend_Debug::dump($users);
    	
    	$auth = Zend_Auth::getInstance();
    	$user = $auth->getIdentity();
    	$this->view->user = $user;
    	
    	$db = Zend_Registry::get('db');
    	$adapter = new Zend_Paginator_Adapter_DbSelect(
    		$db->select()
    			->from('users')
    	);
    	$paginator = new Zend_Paginator($adapter);
    	$paginator->setItemCountPerPage(20)
    		->setCurrentPageNumber($page);
    	//Zend_Debug::dump($paginator);
    	$this->view->paginator = $paginator;
    }
    public function addAction()
    {
    	$jquery = $this->view->jQuery();
        $jquery->enable();
        $jquery->uiEnable();
    	$this->view->jQuery()->addOnLoad(
    		'$( "#date_of_birth" ).datepicker({
    			dateFormat: "dd.mm.yy",
    			firstDay: 1,
    			changeYear: true,
    			changeMonth: true,
    			maxDate: "+1y",
    			yearRange: "1950:c",
    			monthNamesShort: ["Leden","Únor","Březen","Duben","Květen","Červen","Červenec","Srpen","Září","Říjen","Listopad","Prosinec"],
    			dayNamesMin: ["Ne","Po","Út","St","Čt","Pá","So"]
    		 });
    		 $("form :input.tool-tip").tooltip({
        		effect: "fade",
        		position: "center right",
    			opacity: 0.8,
    			predelay: 500
    		 });'
    	);
    	
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css');
    	
    	$form = new Admin_Form_Marketing_Users_Add();
    	$form->setAction($this->view->url(array(), 'admin_marketing_user-add'));
    	$form->setAttrib('id', 'form-add-user');
    	$this->view->form = $form;
    	
    	$usersTypesTab = new Admin_Model_DbTable_UsersTypes();
    	$options = $usersTypesTab->getTypesPairs();
    	$form->type
    		->setMultiOptions($options)
    		->setValue(2);
    	
    	$form->sex
    		->setMultiOptions(array(
    			NULL	=>	'Vyberte...',
    			0		=>	'Muž',
    			1		=>	'Žena'
    	));
    	
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    		
	    	if ($form->isValid($formData))
	    	{
    			$usersTab = new Admin_Model_DbTable_Users();
    			$usersTab->addUser(
    				$formData['type'],
    				$formData['username'],
    				$formData['password'],
    				$formData['email'],
    				$formData['name'],
    				$formData['surname'],
    				$formData['date_of_birth'],
    				$formData['sex']
    			);
	    		
	    		$this->_helper->redirector->gotoRoute(array(), 'admin_marketing_users');
	    	}
        }
    }
    public function editAction()
    {
    	$jquery = $this->view->jQuery();
        $jquery->enable();
        $jquery->uiEnable();
    	$this->view->jQuery()->addOnLoad(
    		'$( "#date_of_birth" ).datepicker({
    			dateFormat: "dd.mm.yy",
    			firstDay: 1,
    			changeYear: true,
    			changeMonth: true,
    			maxDate: "+1y",
    			yearRange: "1950:c",
    			monthNamesShort: ["Leden","Únor","Březen","Duben","Květen","Červen","Červenec","Srpen","Září","Říjen","Listopad","Prosinec"],
    			dayNamesMin: ["Ne","Po","Út","St","Čt","Pá","So"]
    		 });
    		 $("form :input.tool-tip").tooltip({
        		effect: "fade",
        		position: "center right",
    			opacity: 0.8,
    			predelay: 500
    		 });'
    	);
    	
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css');
    	
    	$id = $this->_getParam('id');
    	
    	$form = new Admin_Form_Marketing_Users_Add();
    	$form->setAction($this->view->url(array(), 'admin_marketing_user-edit'));
    	$form->setAttrib('id', 'form-add-user');
    	$this->view->form = $form;
    	
    	$usersTypesTab = new Admin_Model_DbTable_UsersTypes();
    	$options = $usersTypesTab->getTypesPairs();
    	$form->type
    		->setMultiOptions($options);
    	
    	$form->sex
    		->setMultiOptions(array(
    			NULL	=>	'Vyberte...',
    			0		=>	'Muž',
    			1		=>	'Žena'
    	));
    	
    	$form->password
    		->setRequired(false)
    		->setDescription('');
    	
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    		
	    	if ($form->isValid($formData))
	    	{
    			$usersTab = new Admin_Model_DbTable_Users();
    			$usersTab->updateUser(
    				$id,
    				$formData['type'],
    				$formData['username'],
    				$formData['password'],
    				$formData['email'],
    				$formData['name'],
    				$formData['surname'],
    				$formData['date_of_birth'],
    				$formData['sex']
    			);
	    		
	    		$this->_helper->redirector->gotoRoute(array(), 'admin_marketing_users');
	    	}
        }else{
        	$usersTab = new Admin_Model_DbTable_Users();
    		$user = $usersTab->getUser($id);
        	$date = new DateTime($user['date_of_birth']);
    		if ($date){
				$date = $date->format('d.m.Y');
			}
        	$data = array(
				'type'			=>	$user['users_types_id'],
				'username'		=>	$user['username'],
        		'email'			=>	$user['email'],
				'name'			=>	$user['name'],
				'surname'		=>	$user['surname'],
				'date_of_birth'	=>	$date,
				'sex'			=>	$user['sex']
			);
			$form->populate($data);
        }
    }
    public function delAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css');
    	
    	$id = $this->_getParam('id');
    	
    	$form = new Admin_Form_Marketing_Users_Del();
    	$form->setAction($this->view->url(array('id' => $id), 'admin_marketing_user-del'));
    	$this->view->form = $form;
    	
    	$usersTab = new Admin_Model_DbTable_Users();
    	
    	$user = $usersTab->getUser($id);
    	$this->view->username = $user['username'];
    	
    	if ($this->getRequest()->isPost()) {
            $delete = $this->getRequest()->getPost('delete');
                        
            if ($delete != NULL) {
            	$usersTab->setFlag($id, 'delete');
          	}
            $this->_helper->redirector->gotoRoute(array(), 'admin_marketing_users');
    	}
    }
}