<?php

class Auth_IndexController extends Zend_Controller_Action
{
	public function init()
    {
    	$this->_helper->layout()->setLayout('eshop');
    	$this->_helper->eshop->initLayout();
    }
    public function myAccountAction()
    {
    	$auth = Zend_Auth::getInstance();
    	if(!$auth->hasIdentity()){
    		$this->_helper->redirector->gotoRoute(array(), 'auth_index_login');
    	}
    	$user = $auth->getIdentity();
    	$this->view->user = $user;
    	//Zend_Debug::dump($user);
    	
    }
	public function loginAction()
    {
    	$this->view->headLink()->prependStylesheet('/css/form.css');
    	
    	$formLogin = new Auth_Form_Login();
	    $formLogin->setAttrib('id', 'form-login');
	    $this->view->formLogin = $formLogin;
	    
	    if (APPLICATION_ENV == 'development'){
	    	$formLogin->username->setValue('RostaG');
	    }
	    
	    $formRegistration = new Auth_Form_Registration();
    	$formRegistration->setAttrib('id', 'form-registration');
    	$this->view->formRegistration = $formRegistration;
	   	
    	$formGenPSWD = new Auth_Form_GeneratePassword();
	    $formGenPSWD->setAttrib('id', 'form-generatePassword');
	    $this->view->formGenPSWD = $formGenPSWD;
    	
	    if ($this->getRequest()->isPost()){
    		$login = ($this->getRequest()->getPost('login'))?true:false;
	    	if($login){
		    	if ($formLogin->isValid($this->getRequest()->getPost())){
					$username = $this->getRequest()->getPost('username');
		    		//Zend_Debug::dump($username, $label='$username: ', $echo=true);
		       		$pwd = $this->getRequest()->getPost('password');
		   			//Zend_Debug::dump($pwd, $label='$pwd: ', $echo=true);
		   			$auth = Zend_Auth::getInstance();
		   			$authAdapter = new Auth_Model_LoginAdapter($username, $pwd);
		   			$result = $auth->authenticate($authAdapter);
		   			
		   			if(!$result->isValid()){
		   				switch ($result->getCode()){
		    				case Zend_Auth_Result::FAILURE_CREDENTIAL_INVALID:$this->view->loginError = 'Přihlašovací údaje nenalezeny';
		    			}
		    		}else{
		    			$this->_helper->redirector->gotoRoute(array(), 'auth_index_my-account');
		       		}
		     	}
	    	}
	    	$registration = ($this->getRequest()->getPost('registration'))?true:false;
	    	if($registration){
	    		$formData = $this->getRequest()->getPost();
	    		if ($formRegistration->isValid($formData)){
	    			$usersTab = new Auth_Model_DbTable_Users();
		        	if ($usersTab->isUser($formData['username'])) {
		                $this->view->registrationError = 'Uživatelské jméno "'.$formData['username'].'" již existuje.';
		            } else {
		                $this->view->registrationEmail = true;
		                $regTab = new Auth_Model_DbTable_Registrations();
		                $regId = $regTab->setRegistration(
		                	$formData['username'],
		                	$formData['email']
		                );
		                $registration = $regTab->getRegistration($regId);
		                // Poslat email
		                $settingsTab = new Admin_Model_DbTable_Settings();
		                $smtp = $settingsTab->getFlag('smtp');
		                //Zend_Debug::dump($smtp);
		                $config = array(
		                	'auth'		=>	'login',
					    	'username'	=>	$smtp['username'],
					    	'password'	=>	$smtp['password'],
					    	'ssl'		=>	$smtp['ssl'],
					    	'port'		=>	$smtp['port']
		                );
					    $transport = new Zend_Mail_Transport_Smtp($smtp['smtp'], $config);
					    $eshop = $settingsTab->getFlag('eshop');
					    $mail = new Zend_Mail('UTF-8');
						$mail->setHeaderEncoding(Zend_Mime::ENCODING_BASE64);
						$mail->setBodyHtml(
							'Vážený uživateli '.$formData['username'].',<br />'.
							'vítáme Vás na serveru <a href="'.$eshop['url'].'">'.$eshop['title'].'</a>.<br />'.
							'<br />'.
							'Pro dokončení registrace, klikněte na následující odkaz:<br />'.
							'<a href="http://'.$eshop['url'].'/muj-ucet/registrace/token/'.$registration['token'].'"><b>Dokončit registraci</b></a><br />'.
							'<br />'.
							'<br />'.
							'S přáním hezkého dne<br />'.
							'Web: <a href="http://'.$eshop['url'].'/">'.$eshop['title'].'</a>'.'<br />'.
							'Email: <a href="mailto:'.$eshop['email'].'">'.$eshop['email'].'</a>'
							,
							'UTF-8', 'UTF-8'
						);
						$mail->setFrom($eshop['email'], $eshop['title']);
						$mail->addTo($formData['email'], $formData['username']);
						$mail->setSubject('Registrace - '.$eshop['title']);
						$mail->send($transport);
						
						$this->_helper->redirector->gotoRoute(array(), 'auth_index_registration');
			    	}
	    		}
	    	}
	    	$genPSWD = ($this->getRequest()->getPost('generatepassword'))?true:false;
	    	if($genPSWD){
		    	$formData = $this->getRequest()->getPost();
		    	//Zend_Debug::dump($formData, $label='$formData: ', $echo=true);
		    	if ($formGenPSWD->isValid($formData))
		        {
		            $usersTab = new Auth_Model_DbTable_Users();
		            $user = $usersTab->getUserByEmail($formData['email']);
		            //Zend_Debug::dump($user);
		            if (!$user) {
		                $this->view->genPswdError = 'Tento email neodpovídá žádnému záznamu.';
		            } else {
		                $newPaswd = $usersTab->genetarePassword();
		                $usersTab->updateUser(
		                	$user['id'],
		                	$user['username'],
		                	$newPaswd,
		                	$user['email']
		                );
		                // Poslat email
		                $settingsTab = new Admin_Model_DbTable_Settings();
			            $smtp = $settingsTab->getFlag('smtp');
			            //Zend_Debug::dump($smtp);
			            $config = array(
			            	'auth'		=>	'login',
			  				'username'	=>	$smtp['username'],
			   				'password'	=>	$smtp['password'],
			   				'ssl'		=>	$smtp['ssl'],
			   				'port'		=>	$smtp['port']
						);
						$transport = new Zend_Mail_Transport_Smtp($smtp['smtp'], $config);
						$eshop = $settingsTab->getFlag('eshop');
						$mail = new Zend_Mail('UTF-8');
						$mail->setHeaderEncoding(Zend_Mime::ENCODING_BASE64);
						$mail->setBodyHtml(
							'Vážený uživateli '.$user['username'].',<br />'.
				    		'vítáme Vás na serveru <a href="'.$eshop['url'].'">'.$eshop['title'].'</a>.<br />'.
				    		'<br />'.
				    		'tento email Vám byl zaslán, jelikož jste požádal o obnovu hesla.'.'<br />'.
				    		'<br />'.
	    					'<b>Přihlašovací údaje:</b>'.'<br />'.
	    					'Uživatelské jméno: <b>'.$user['username'].'</b>'.'<br />'.
	    					'Vaše nové heslo je: <b>'.$newPaswd.'</b>'.'<br />'.
	    					'<br />'.
	    					'<b>Nově přidělené heslo si můžete změnit po přihlášení ve svém účtu.</b>'.'<br />'.
	    					'Pokud máte jakékoliv otázky či připomínky, neváhejte nás kontaktovat.'.'<br />'.
	    					'Děkujeme a věříme, že s námi budete spokojeni.'.'<br />'.
	    					'<br />'.
				    		'S přáním hezkého dne<br />'.
				    		'Web: <a href="http://'.$eshop['url'].'/">'.$eshop['title'].'</a>'.'<br />'.
				    		'Email: <a href="mailto:'.$eshop['email'].'">'.$eshop['email'].'</a>'
				    		,
				    		'UTF-8', 'UTF-8'
				    	);
						$mail->setFrom($eshop['email'], $eshop['title']);
						$mail->addTo($formData['email'], $user['username']);
						$mail->setSubject('Obnova hesla - '.$eshop['title']);
						$mail->send($transport);
						
						$this->_helper->redirector->gotoRoute(array(), 'auth_index_password-generated');
			       	}
				}
		    }
	    }
    }
    public function registrationAction()
    {
    	
    }
	public function registrationContinueAction()
    {
    	$this->view->headLink()->prependStylesheet('/css/form.css');
    	
    	$formRegEnd = new Auth_Form_RegistrationEnd();
	    $formRegEnd->setAttrib('id', 'form-registrationEnd');
	    $this->view->formRegistrationEnd = $formRegEnd;
	    
	    if ($this->getRequest()->isPost()){
    		$registrationEnd = ($this->getRequest()->getPost('registrationend'))?true:false;
	    	if($registrationEnd){
	    		$formData = $this->getRequest()->getPost();
		    	//Zend_Debug::dump($formData, $label='$formData: ', $echo=true);
		    	if ($formRegEnd->isValid($formData)){
		        	$regTab = new Auth_Model_DbTable_Registrations();
		        	$token = $this->getRequest()->getParam('token');
		        	$registration = $regTab->getRegistrationByToken($token);
		        	//Zend_Debug::dump($registration);
		        	if ($registration){
		        		$regTab->delRegistration($registration['id']);
		        		
		        		$usersTab = new Auth_Model_DbTable_Users();
		        		$usersTab->addUser($registration['username'],
		                				   $formData['password'],
		                				   $registration['email']);
		                
		                //poslat email
		                $settingsTab = new Admin_Model_DbTable_Settings();
		                $smtp = $settingsTab->getFlag('smtp');
		                //Zend_Debug::dump($smtp);
		                $config = array('auth'		=>	'login',
					    				'username'	=>	$smtp['username'],
					    				'password'	=>	$smtp['password'],
					    				'ssl'		=>	$smtp['ssl'],
					    				'port'		=>	$smtp['port']);
					    $transport = new Zend_Mail_Transport_Smtp($smtp['smtp'], $config);
					    $eshop = $settingsTab->getFlag('eshop');
					    $mail = new Zend_Mail('UTF-8');
						$mail->setHeaderEncoding(Zend_Mime::ENCODING_BASE64);
						$mail->setBodyHtml('Vážený uživateli '.$registration['username'].',<br />'.
							    			'vítáme Vás na serveru <a href="'.$eshop['url'].'">'.$eshop['title'].'</a>.<br />'.
							    			'<br />'.
							    			'Vaše registrace byla úspěšně dokončena:<br />'.
							    			'Uživatelské jméno: '.$registration['username'].'<br />'.
											'Heslo: '.$formData['password'].'<br />'.
							    			'<br />'.
							    			'<br />'.
							    			'S přáním hezkého dne<br />'.
							    			'Web: <a href="http://'.$eshop['url'].'/">'.$eshop['title'].'</a>'.'<br />'.
							    			'Email: <a href="mailto:'.$eshop['email'].'">'.$eshop['email'].'</a>'
							    			,
							    			'UTF-8', 'UTF-8');
						$mail->setFrom($eshop['email'], $eshop['title']);
						$mail->addTo($registration['email'], $registration['username']);
						$mail->setSubject('Registrace - '.$eshop['title']);
						$mail->send($transport);
						
						$this->_helper->redirector->gotoRoute(array(), 'auth_index_registration-done');
		    	  	}else{
		    	  		$this->view->registrationNotDone = true;
		    	  	}
    			}
	    	}
	    }
    }
    public function registrationDoneAction()
    {
    	
    }
    public function passwordChangeAction()
    {
    	$auth = Zend_Auth::getInstance();
    	if(!$auth->hasIdentity()){
    		$this->_helper->redirector->gotoRoute(array(), 'auth_index_login');
    	}
    	
    	$this->view->headLink()->prependStylesheet('/css/form.css');
    	
    	$formChangePSWD = new Auth_Form_ChangePassword();
	    $formChangePSWD->setAttrib('id', 'form-changePassword');
	    $this->view->formChangePSWD = $formChangePSWD;
	    
	     if ($this->getRequest()->isPost()){
    		$changePswd = ($this->getRequest()->getPost('changePswd'))?true:false;
	    	if($changePswd){
		    	$formData = $this->getRequest()->getPost();
				//Zend_Debug::dump($formData);
				
				if ($formChangePSWD->isValid($formData))
		        {
		        	$user = $auth->getIdentity();
		        	$usersTab = new Auth_Model_DbTable_Users();
		            $usersTab->updateUser(
		            	$user['id'],
		                $user['username'],
		                $formData['password'],
		                $user['email']
		               );
					// Poslat email
		            $settingsTab = new Admin_Model_DbTable_Settings();
			        $smtp = $settingsTab->getFlag('smtp');
			        //Zend_Debug::dump($smtp);
			        $config = array(
			        	'auth'		=>	'login',
					  	'username'	=>	$smtp['username'],
					   	'password'	=>	$smtp['password'],
					   	'ssl'		=>	$smtp['ssl'],
					   	'port'		=>	$smtp['port']
					);
					$transport = new Zend_Mail_Transport_Smtp($smtp['smtp'], $config);
					$eshop = $settingsTab->getFlag('eshop');
					$mail = new Zend_Mail('UTF-8');
					$mail->setHeaderEncoding(Zend_Mime::ENCODING_BASE64);
				    $mail->setBodyHtml(
				    	'Vážený uživateli '.$this->user['username'].',<br />'.
						'vítáme Vás na serveru <a href="'.$eshop['url'].'">'.$eshop['title'].'</a>.<br />'.
						'<br />'.
						'tento email Vám byl zaslán, jelikož jste změnil své heslo.'.'<br />'.
						'<br />'.
					   	'<b>Přihlašovací údaje:</b>'.'<br />'.
					   	'Uživatelské jméno: <b>'.$user['username'].'</b>'.'<br />'.
					   	'Vaše nové heslo je: <b>'.$formData['password'].'</b>'.'<br />'.
					   	'<br />'.
					   	'<b>Změněné heslo si můžete změnit po přihlášení ve svém účtu.</b>'.'<br />'.
					   	'Pokud máte jakékoliv otázky či připomínky, neváhejte nás kontaktovat.'.'<br />'.
					   	'Děkujeme a věříme, že s námi budete spokojeni.'.'<br />'.
					   	'<br />'.
						'S přáním hezkého dne<br />'.
						'Web: <a href="http://'.$eshop['url'].'/">'.$eshop['title'].'</a>'.'<br />'.
						'Email: <a href="mailto:'.$eshop['email'].'">'.$eshop['email'].'</a>'
						,
						'UTF-8', 'UTF-8'
					);
					$mail->setFrom($eshop['email'], $eshop['title']);
					$mail->addTo($user['email'], $user['username']);
					$mail->setSubject('Změna hesla - '.$eshop['title']);
					$mail->send($transport);
					    
				    $authAdapter = new Auth_Model_LoginAdapter($user['username'],$formData['password']);
		            $auth = Zend_Auth::getInstance();
			       	$auth->authenticate($authAdapter);
			       	
			       	$this->_helper->redirector->gotoRoute(array(), 'auth_index_password-changed');
		        }
	    	}
		}
	}
	public function passwordChangedAction()
    {
    	
    }
    public function passwordGeneratedAction()
    {
    	
    }
    public function logoutAction()
    {
    	$auth = Zend_Auth::getInstance();
		$auth->clearIdentity();

		$this->_helper->redirector->gotoRoute(array(), 'eshop_index_index');
    }
}