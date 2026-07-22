<?php
class Auth_Form_Profile_Edit extends Zend_Form
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
    	$this->addElement('text', 'email', array(
    		'decorators'	=>	$this->elementDecorators,
    		'label'		=>	'Email *',
    		'required'	=>	true,
    		'size'			=>	40,
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
        																  		
        $this->addElement('text', 'name', array(
        	'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Jméno',
        	'size'			=>	40,
        	'validators'	=>	array(
        		array(
        			'Alnum',
					true,
					array('messages'	=>	array('notAlnum'	=>	"'%value%' může obsahovat pouze písmena a čísla"))
				)
			)
		));
        
		$this->addElement('text', 'surname', array(
			'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Příjmení',
        	'size'			=>	40,
        	'validators'	=>	array(
				array(
					'Alnum',
					true,
					array('messages'	=>	array('notAlnum'	=>	"'%value%' může obsahovat pouze písmena a čísla"))
				)
			)
		));
													  				 
		$this->addElement('text', 'date_of_birth', array(
			'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Datum narození',
        	'size'			=>	40,
        	'validators'	=>	array(
				array(
					'Date',
					true,
					array(
						'format'	=>	'dd.mm.yyyy',
						'messages'	=>	array(
							'dateInvalid'		=>	"Invalid type given, value should be string, integer, array or Zend_Date",
							'dateInvalidDate'	=>	"'%value%' není validní datum, podle formátu 'RRRR-MM-DD'",
							'dateFalseFormat'	=>	"'%value%' nesouhlasí s formátem 'RRRR-MM-DD'"
						)
					)
				)
			)
		));
													  				 
		$this->addElement('select', 'sex', array(
			'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Pohlaví',
        	'size'			=>	1
		));
		
        $this->addElement('submit', 'saveprofile', array(
        	'decorators'	=>	$this->buttonDecorators,
        	'label'			=>	'Uložit'
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
        			'id'	=>	'form-edit-profile'
        		)
        	),
        	array('Form')
        ));
    }
}