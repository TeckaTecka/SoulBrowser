<?php
class Default_Form_Request extends Zend_Form
{
	private $elementDecorators = array(
		'ViewHelper',
		array('Label', array('separator' => '')), 
		array(
			'Description',
			array('tag'		=>	'span')
		),
		'Errors',
		array(
			'HtmlTag', array(
				'tag'	=>	'div',
				'class'	=>	'element-form'
			)
		)
	);
	
    public $buttonDecorators = array('ViewHelper');
    								 	   
	public function init()
    {
    	$this->addElement('text', 'name', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Jméno',
        	'description'	=>	'*',
    		'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'Jméno musí být vyplněno'))
				)
			)
		));

		$this->addElement('text', 'email', array(
    		'decorators'	=>	$this->elementDecorators,
    		'label'			=>	'Email',
    		'required'		=>	true,
    		'description'	=>	'*',
    		'validators'	=>	array(
    			array(
    				'NotEmpty',
    				true,
    				array('messages'	=>	array('isEmpty'		=>	'Email musí být vyplněn'))
    			),
    			array(
    				'EmailAddress',
    				true,
    				array(
    					'messages'		=>	array(
    						'emailAddressDotAtom'			=>	"xxx1",
    						'emailAddressInvalid'			=>	"xxx2",
        					'emailAddressInvalidFormat'		=>	"'%value%' není validní emailová adresa ve formátu local-part@hostname",
        					'emailAddressInvalidHostname'	=>	"'%hostname%' není validní hostname pro emailovou adresu '%value%'",
        					'emailAddressInvalidLocalPart'	=>	"xxx5",
        					'emailAddressInvalidMxRecord'	=>	"xxx6",
        					'emailAddressInvalidSegment'	=>	"xxx7",
        					'emailAddressLengthExceeded'	=>	"xxx8",
        					'emailAddressQuotedString'		=>	"xxx9",
        					'hostnameInvalidHostname'		=>	"%value%' se nezhoduje se strukturou pro DNS hostname",
        					'hostnameLocalNameNotAllowed'	=>	"'%value%' vypadá jako lokální název sítě ale lokální názvy sítí nejsou povoleny",
        					'hostnameUndecipherableTld'		=>	"'%value%' vypadá jako DNS hostname ale nelze extrahovat TLD část",
        					'hostnameInvalidLocalName'		=>	"'%value%' nezdá se být lokálním platným názvem sítě",
        					'hostnameUnknownTld'			=>	"'%value%' zdá se být email ale nesouhlasí TLD část"
    					)
    				)
    			)
    		)
    	));
        																  		
        $this->addElement('text', 'company', array(
        	'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Společnost'
        ));
        
		$this->addElement('text', 'phone', array(
    		'decorators'	=>	$this->elementDecorators,
	        'label'			=>	'Kontaktní telefon',
			'description'	=>	'*',
			'required'		=>	true,
			'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'Telefon musí být vyplněn'))
				)
			)
		));
		
		$this->addElement('textarea', 'message', array(
    		'decorators'	=>	$this->elementDecorators,
	        'label'			=>	'Zpráva',
			//'description'	=>	'*',
			'required'		=>	true,
			'rows'			=>	30,
			'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'Zpráva musí být vyplněna'))
				)
			)
		));
		
        $this->addElement('submit', 'send', array(
        	'decorators'	=>	$this->buttonDecorators,
        	'label'			=>	'Odeslat'
        ));
       
        
    }

	public function loadDefaultDecorators()
    {
    	$this->setDecorators(array(
    		'FormElements',
        	array(
        		'HtmlTag',
        		array(
        			'tag'	=>	'div',
        			'id'	=>	'form-request'
        		)
        	),
        	array('Form')
        ));
    }
}