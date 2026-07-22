<?php

class QuickContact_EshopController extends Zend_Controller_Action
{
	public function init()
    {
    	$this->_helper->layout()->setLayout('eshop');
    	$this->_helper->eshop->initLayout(); 
    }
	public function indexAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/shared/form.css');
    	
    	$form = new QuickContact_Form_Request();
    	$form->setAction($this->view->url(array(), 'quick-contact_eshop_index'));
    	$form->setAttrib('id', 'form-quick-contact');
    	$this->view->form = $form;
    	
    	if (($this->getRequest()->isPost()) AND ($this->getRequest()->getPost('quickContactSend')))
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    		
	    	if ($form->isValid($formData))
	    	{
    			// Poslat email
                $settingsTab = new QuickContact_Model_DbTable_Settings();
                $smtp = $settingsTab->getFlag('smtp');
                $eshop = $settingsTab->getFlag('eshop');
                $quickContact = $settingsTab->getFlag('quick-contact');
                //Zend_Debug::dump($smtp);
                $config = array('auth'		=>	'login',
			    				'username'	=>	$smtp['username'],
			    				'password'	=>	$smtp['password'],
			    				'ssl'		=>	$smtp['ssl'],
			    				'port'		=>	$smtp['port']);
			    $transport = new Zend_Mail_Transport_Smtp($smtp['smtp'], $config);
			    
			    $mail = new Zend_Mail('UTF-8');
				$mail->setHeaderEncoding(Zend_Mime::ENCODING_BASE64);
				$mail->setBodyHtml(
					'Zpráva z '.$eshop['title'].',<br />'.
					'<br />'.
					'Jméno: '.$formData['name'].'<br />'.
					'Email: '.$formData['email'].'<br />'.
					'Společnost: '.$formData['company'].'<br />'.
					'Telefon: '.$formData['phone'].'<br />'.
					'Zpráva:<br />'.$formData['message']
					,
					'UTF-8',
					'UTF-8'
				);
				$mail->setFrom($eshop['email'], $eshop['title']);
				$mail->addTo($quickContact['email'], $quickContact['title']);
				$mail->setSubject('Rychlý kontakt - '.$eshop['title']);
				$mail->send($transport);
	    		
	    		$this->_helper->redirector->gotoRoute(array(), 'quick-contact_eshop_sent');
	    	}
        }
    }
    public function sentAction()
    {
    	
    }
}