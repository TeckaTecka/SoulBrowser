<?php
class Auth_Form_GeneratePassword extends Zend_Form
{
	public $elementDecorators = array('ViewHelper',
									  array('Errors', array('class'=>'errors')),
									  'Label',
									  array(array('row' => 'HtmlTag'),
							  			    array('tag' => 'div'))
							  		  );
	
    public $buttonDecorators = array('ViewHelper');

    public function init()
    {
        $this->addElement('text', 'email', array('decorators'	=>	$this->elementDecorators,
        										 'label'		=>	'Váš email',
												 'required'	=>	true,
        										 'size'			=>	40,
        										 'validators'	=>	array(array('NotEmpty',
        																		true,
        																		array('messages'=>array('isEmpty'=>'Email musí být vyplněn'))),
        																  array('EmailAddress',
        																  		true,
        																  		array('messages'=>array('emailAddressDotAtom'=>"xxx1",
        																  								'emailAddressInvalid'=>"xxx2",
        																  								'emailAddressInvalidFormat'=>"'%value%' není validní emailová adresa ve formátu local-part@hostname",
        																  								'emailAddressInvalidHostname'=>"'%hostname%' není validní hostname pro emailovou adresu '%value%'",
        																  								'emailAddressInvalidLocalPart'=>"xxx5",
        																  								'emailAddressInvalidMxRecord'=>"xxx6",
        																  								'emailAddressInvalidSegment'=>"xxx7",
        																  								'emailAddressLengthExceeded'=>"xxx8",
        																  								'emailAddressQuotedString'=>"xxx9",
        																  								'hostnameInvalidHostname'=>"%value%' se nezhoduje se strukturou pro DNS hostname",
        																  								'hostnameLocalNameNotAllowed'=>"'%value%' vypadá jako lokální název sítě ale lokální názvy sítí nejsou povoleny",
        																  								'hostnameUndecipherableTld'=>"'%value%' vypadá jako DNS hostname ale nelze extrahovat TLD část",
        																  								'hostnameInvalidLocalName'=>"'%value%' nezdá se být lokálním platným názvem sítě",
        																  								'hostnameUnknownTld'=>"'%value%' zdá se být email ale nesouhlasí TLD část"))))));
		$this->addElement('submit', 'generatepassword', array('decorators'	=>	$this->buttonDecorators,
        										  	'label'			=>	'Odeslat'));
    }

	public function loadDefaultDecorators()
    {
    	$this->setDecorators(array('FormElements',
        						   array('HtmlTag',
        						   		 array('tag' => 'div', 'id' => 'form-auth-generatePassword'/*, 'style'=>'width: 300px;margin: auto;'*/)),
        						   array('Form')));
    }
}