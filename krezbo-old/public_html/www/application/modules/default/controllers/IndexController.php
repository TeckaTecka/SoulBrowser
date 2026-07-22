<?php

class IndexController extends Zend_Controller_Action
{

    public function init()
    {
        
    }

    public function indexAction()
    {
        
    }
	public function requestAction()
    {
    	$this->view->headLink()
    		->prependStylesheet('/css/form.css');
    	
    	$form = new Default_Form_Request();
    	$form->setAction($this->view->url(array(), 'default_index_request'));
    	$form->setAttrib('id', 'form-request');
    	$this->view->form = $form;
    	
    	if ($this->getRequest()->isPost())
    	{
    		$formData = $this->getRequest()->getPost();
	    	//Zend_Debug::dump($formData);
	    		
	    	if ($form->isValid($formData))
	    	{
    			// Poslat email
                $settingsTab = new Default_Model_DbTable_Settings();
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
				$mail->addTo($eshop['email'], $eshop['title']);
				$mail->setSubject('Poptávka - '.$eshop['title']);
				$mail->send($transport);
	    		
	    		$this->_helper->redirector->gotoRoute(array(), 'default_index_request-sent');
	    	}
        }
    }
    public function requestSentAction()
    {
    	
    }
	public function galleryAction()
    {
        $this->view->headLink()->prependStylesheet('/css/prettyphoto/prettyphoto.css');
    	$this->view->headScript()->appendFile('/js/jquery/jquery.prettyphoto.js');    	
    	$this->view->jQuery()->addOnLoad(
    		'$(".gallery a[rel^='."'prettyPhoto'".']").prettyPhoto({
    			animationSpeed:'."'slow'".',
    			theme:'."'light_rounded'".',
    			slideshow:5000
    		 });');
    }
	public function contactAction()
    {
        
    }
	public function sitemapAction()
    {
        
    }
}

