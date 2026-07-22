<?php
class Auth_Form_Registration extends Zend_Form
{
	public $elementDecorators = array('ViewHelper',
									  
									  'Label',
									  array('Description',
	                                        array('tag' => 'span')),
	                                  array('Errors', array('class'=>'errors')),
									  array(array('row' => 'HtmlTag'),
							  			    array('tag' => 'div')),
							  		  
							  		  );

    public $buttonDecorators = array('ViewHelper');

    public function init()
    {
        $this->addElement('text', 'username', array('decorators'	=>	$this->elementDecorators,
        										 'label'		=>	'Login',
                                                 'description'	=>    '6 až 20 znaků',
        										 'size'			=>	40,
        										 'required'		=>	true,
        										 'validators'	=>	array(array('NotEmpty',
													  							 true,
													  							 array('messages'=>array('isEmpty'=>'Login musí být vyplněn'))
													  					   		),
																		   array('Alnum',
													  							 true,
													  							 array('messages'=>array('notAlnum'=>"'%value%' může obsahovat pouze písmena a čísla"))
													  				 			),
													  				 	   array('stringLength',
													  							 true,
													  							 array(array('min'=>6, 'max'=>20),
													  							 	   'messages'=>array('stringLengthTooShort'=>"'%value%' je kratší než %min% znaků",
													  							 						 'stringLengthTooLong'=>"'%value%' je delší než %max% znaků")
													  							 	  )
													  				 			)
													  				 	  )
													  				 ));
       $this->addElement('text', 'email', array('decorators'	=>	$this->elementDecorators,
        										 'label'		=>	'Email',
		                                         'description'	=>    'např.: jmeno@email.cz',
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
       $this->addElement('submit', 'registration', array('decorators'	=>	$this->buttonDecorators,
        										  	'label'			=>	'Registrovat'));
    }

	public function loadDefaultDecorators()
    {
    	$this->setDecorators(array('FormElements',
        						   array('HtmlTag',
        						   		 array('tag' => 'div', 'id' => 'form-auth-registration')),//, 'style'=>'width: 100%;')),
        						   array('Form'),));
    }
}