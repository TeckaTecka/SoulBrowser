<?php

class Auth_ProfileController extends Zend_Controller_Action
{
	public function init()
    {
    	$auth = Zend_Auth::getInstance();
    	if(!$auth->hasIdentity()){
    		$this->_helper->redirector->gotoRoute(array(), 'auth_index_login');
    	}else{
    		$this->_helper->layout()->setLayout('eshop');
    		$this->_helper->eshop->initLayout();
    	}
    }
	public function editAction()
    {
    	$this->view->headLink()->prependStylesheet('/css/form.css');
    	
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
    		 });'
    	); 
    	
    	$form = new Auth_Form_Profile_Edit();
    	$form->setAction($this->view->url(array(),'auth_profile_edit'));
    	$this->view->form = $form;
    	$form->sex->setMultiOptions(array(NULL	=>	'Vyberte...',
    									  0		=>	'Muž',
    									  1		=>	'Žena'));
		$auth = Zend_Auth::getInstance();
    	$user = $auth->getIdentity();
    	if (($this->getRequest()->isPost() AND ($this->getRequest()->getPost('saveprofile')))){
    		$formData = $this->getRequest()->getPost();
    		//Zend_Debug::dump($formData);
    		if ($form->isValid($formData)){
    			$usersTab = new Auth_Model_DbTable_Users();
    			$usersTab->updateUserInfo($user['id'],
    									  $formData['email'],
    									  $formData['name'],
    									  $formData['surname'],
    									  $formData['date_of_birth'],
    									  $formData['sex']);
    			
    			$authAdapter = new Auth_Model_LoginAdapter($user['username'], null);
   				$result = $auth->authenticate($authAdapter);
	    		if(!$result->isValid()) {
		    		switch ($result->getCode()){
	    				case Zend_Auth_Result::FAILURE_CREDENTIAL_INVALID:$this->view->information = 'Ukládání selhalo';
	    			}
		        } else {
		            //Zend_Debug::dump($user);
		            $this->view->information = 'Ukládání proběhlo v pořádku';
		        }
        	}
    	}else{
    		
    		//Zend_Debug::dump($user);
    		$date = new DateTime($user['date_of_birth']);
    		//$date = DateTime::createFromFormat('Y-m-d', $user['date_of_birth']);
    		//$date = date_create_from_format('Y-m-d', $user['date_of_birth']);
    		if ($date){
				$date = $date->format('d.m.Y');
				//$date = date_format($date,'d.m.Y');
				//Zend_Debug::dump($date);
    		}
			$data = array('email'			=>	$user['email'],
						  'name'			=>	$user['name'],
						  'surname'			=>	$user['surname'],
						  'date_of_birth'	=>	$date,
						  'sex'				=>	$user['sex']);
    		$form->populate($data);
    	}
    }
}