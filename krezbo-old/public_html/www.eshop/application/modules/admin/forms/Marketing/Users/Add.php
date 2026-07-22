<?php
class Admin_Form_Marketing_Users_Add extends Zend_Form
{
	public $elementDecorators = array(
		'ViewHelper',
		array('Label', array('separator'	=>	'')), 
		array('Description', array('tag'	=>	'span')),
		'Errors',
		array('HtmlTag', array('tag'	=>	'div', 'class'	=>	'element'))
	);
	
    public $buttonDecorators = array(
    	'ViewHelper',
    	array('HtmlTag', array('class'	=>	'button'))
    );
    
    public $selectDecorators = array(
		'ViewHelper',
		array('Label', array('separator'	=>	'')), 
		array('Description', array('tag'	=>	'span')),
		'Errors',
		array('HtmlTag', array('tag'	=>	'div', 'class'	=>	'select'))
	);
    								 	   
	public function init()
    {
    	$this->addElement('select', 'type', array(
			'decorators'	=>	$this->selectDecorators,
        	'label'			=>	'Typ',
    		'description'	=>	'*',
    		'class'			=>	'tool-tip',
    		'title'			=>	'Typ uživatele:<br />'.
    							'Admin - nejvyšší oprávnění<br />'.
    							'Registrovaný - bez přístupu k admin prostředí',
        	'required'		=>	true,
        	'size'			=>	1
		));
		
    	$this->addElement('text', 'username', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Login',
        	'description'	=>	'*',
    		'class'			=>	'tool-tip',
    		'title'			=>	'Uživatelské jméno,<br />'.
    							'pod kterým se budete přihlašovat ke svému účtu.<br />'.
    							'5 až 20 znaků, pouze písmena a čísla',
        	'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'Login musí být vyplněn'))
				),
				array(
					'Alnum',
					true,
					array('messages'	=>	array('notAlnum'	=>	"'%value%' může obsahovat pouze písmena a čísla"))
				),
				array(
					'stringLength',
					true,
					array(
						array(
							'min'=>5,
							'max'=>20
						),
						'messages'	=>	array(
							'stringLengthTooShort'	=>	"'%value%' je kratší než %min% znaků",
							'stringLengthTooLong'	=>	"'%value%' je delší než %max% znaků"
						)
					)
				)
			)
		));

		$this->addElement('text', 'password', array(
			'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Heslo',
        	'required'		=>	true,
        	'description'	=>	'*',
			'class'			=>	'tool-tip',
    		'title'			=>	'5 až 20 znaků, pouze písmena a čísla',
        	'validators'	=>	array(
				array(
					'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'Heslo musí být vyplněno'))
				),
				array(
					'Alnum',
					true,
					array('messages'	=>	array('notAlnum'	=>	"'%value%' může obsahovat pouze písmena a čísla"))
				),
				array(
					'stringLength',
					true,
					array(
						array(
							'min'=>5,
							'max'=>20
						),
						'messages'	=>	array(
							'stringLengthTooShort'	=>	"'%value%' je kratší než %min% znaků",
							'stringLengthTooLong'	=>	"'%value%' je delší než %max% znaků"
						)
					)
				)
			)
		));
															  				 
    	$this->addElement('text', 'email', array(
    		'decorators'	=>	$this->elementDecorators,
    		'label'			=>	'Email',
    		'required'		=>	true,
    		'description'	=>	'*',
    		'class'			=>	'tool-tip',
    		'title'			=>	'Váš email, který bude použit pro komunikaci,<br />'.
    							've formátu xxxxx@xxxx.xx',
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
        	'class'			=>	'tool-tip',
    		'title'			=>	'pouze písmena a čísla',
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
        	'class'			=>	'tool-tip',
    		'title'			=>	'pouze písmena a čísla',
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
        	'class'			=>	'tool-tip',
    		'title'			=>	've formátu DD.MM.RRRR',
        	'validators'	=>	array(
				array(
					'Date',
					true,
					array(
						'format'	=>	'dd.mm.yyyy',
						'messages'	=>	array(
							'dateInvalid'		=>	"Invalid type given, value should be string, integer, array or Zend_Date",
							'dateInvalidDate'	=>	"'%value%' není validní datum, podle formátu 'DD.MM.RRRR'",
							'dateFalseFormat'	=>	"'%value%' nesouhlasí s formátem 'DD.MM.RRRR'"
						)
					)
				)
			)
		));
													  				 
		$this->addElement('select', 'sex', array(
			'decorators'	=>	$this->selectDecorators,
        	'label'			=>	'Pohlaví',
        	'size'			=>	1,
			//'class'			=>	'tool-tip',
    		//'title'			=>	''
		));
		
        $this->addElement('submit', 'saveUser', array(
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
        			'id'	=>	'form-user'
        		)
        	),
        	array('Form')
        ));
    }
}