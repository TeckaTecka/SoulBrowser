<?php
class Admin_Form_Options_Eshop_Eshop extends Zend_Form
{
	public $elementDecorators = array(
		'ViewHelper',
		array('Label', array('separator'	=>	'')), 
		array('Description', array('tag'	=>	'span')),
		'Errors',
		array('HtmlTag', array('tag'	=>	'div', 'class'	=>	'element'))
	);
	public $buttonDecorators = array('ViewHelper');
        								 	   
	public function init()
    {
    	$this->addElement('text', 'title', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Název eshopu',
    		'description'	=>	'*',
        	'title'			=>	'Název eshopu musí být vyplněn',
        	'class'			=>	'tool-tip',
        	'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'Název eshopu musí být vyplněn'))
				)
			)
		));
		$this->addElement('text', 'url', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Url adresa',
    		'description'	=>	'*',
        	'title'			=>	'Url adresa musí být vyplněna',
        	'class'			=>	'tool-tip',
        	'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'Url adresa musí být vyplněna'))
				)
			)
		));
		
    	$this->addElement('text', 'email', array(
    		'decorators'	=>	$this->elementDecorators,
    		'label'			=>	'Email',
    		'description'	=>	'* např.: jmeno@email.cz',
    		'title'			=>	'Email musí být vyplněn',
        	'class'			=>	'tool-tip',
    		'required'		=>	true,
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
    					'messages'	=>	array(
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
    	
    	$this->addElement('text', 'street', array(
        	'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Ulice',
        	'description'	=>	'*',
    		'title'			=>	'Ulice musí být vyplněna',
        	'class'			=>	'tool-tip',
			'required'		=>	true,
			'validators'	=>	array(
        		array(
					'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'Ulice musí být vyplněna'))
				)
			)
		));
		
		$this->addElement('text', 'street_nr', array(
        	'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Číslo popisné',
        	'description'	=>	'*',
			'title'			=>	'Číslo popisné musí být vyplněno',
        	'class'			=>	'tool-tip',
			'required'		=>	true,
			'validators'	=>	array(
        		array(
					'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'Číslo popisné popisné musí být vyplněno'))
				)
			)
		));
		
		$this->addElement('text', 'city', array(
			'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Město nebo obec',
			'description'	=>	'*',
			'title'			=>	'Město nebo obec musí být vyplněna',
        	'class'			=>	'tool-tip',
			'required'		=>	true,
			'validators'	=>	array(
				array(
					'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'Město nebo obec musí být vyplněna'))
				)
			)
		));
		
    	$this->addElement('text', 'zip', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'PSČ',
    		'description'	=>	'*',
        	'title'			=>	'PSČ musí být vyplněno',
        	'class'			=>	'tool-tip',
        	'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'PSČ musí být vyplněno'))
				),
				array(
					'Digits',
					true,
					array('messages'	=>	array(
						'notDigits'			=>	"PSČ může obsahovat pouze čísla",
						'digitsStringEmpty'	=>	"PSČ musí být vyplněno"
					))
				),
				array(
					'Regex',
					true,
					array(
						'/^[0-9]{5}/',
						'messages'	=>	array('regexNotMatch'	=>	'PSČ není v platném formátu. Např.: 70030')
					)
				)
			)
		));
		
    	$this->addElement('text', 'country', array(
			'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Země',
			'description'	=>	'*',
    		'title'			=>	'Země musí být vybrána',
    		'class'			=>	'tool-tip',
        	'size'			=>	1,
			'required'		=>	true,
			'validators'	=>	array(
				array(
					'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'Země musí být vybrána'))
				)
			)
		));
		
		$this->addElement('text', 'companyIC', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'IČ',
    		'description'	=>	'*',
			'title'			=>	'IČ musí být vyplněno',
    		'class'			=>	'tool-tip',
        	'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'IČ musí být vyplněno'))
				)
			)
		));
		
    	$this->addElement('text', 'companyDIC', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'DIČ',
    		'title'			=>	'DIČ nemusí být vyplněno',
    		'class'			=>	'tool-tip'
		));
		
		$this->addElement('text', 'phone', array(
    		'decorators'	=>	$this->elementDecorators,
	        'label'			=>	'Telefon',
			'description'	=>	'* Např.: +420123456789',
			'title'			=>	'Telefon musí být vyplněno',
    		'class'			=>	'tool-tip',
			'required'		=>	true,
			'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'Telefon musí být vyplněn'))
				),
				array(
					'Regex',
					true,
					array(
						'/^\+[0-9]{12}/',
						'messages'	=>	array('regexNotMatch'	=>	'Telefon není v platném formátu. Např.: +420123456789')
					)
				)
			)
		));
    	
		$this->addElement('text', 'mobile', array(
    		'decorators'	=>	$this->elementDecorators,
	        'label'			=>	'Mobilní telefon',
			'description'	=>	'* Např.: +420123456789',
			'title'			=>	'Mobilní telefon musí být vyplněno',
    		'class'			=>	'tool-tip',
			'required'		=>	true,
			'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'Mobilní telefon musí být vyplněn'))
				),
				array(
					'Regex',
					true,
					array(
						'/^\+[0-9]{12}/',
						'messages'	=>	array('regexNotMatch'	=>	'Mobilní telefon není v platném formátu. Např.: +420123456789')
					)
				)
			)
		));
		
		$this->addElement('text', 'fax', array(
    		'decorators'	=>	$this->elementDecorators,
	        'label'			=>	'Fax',
			'description'	=>	'* Např.: +420123456789',
			'title'			=>	'Fax musí být vyplněn',
    		'class'			=>	'tool-tip',
			'required'		=>	true,
			'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'Fax musí být vyplněn'))
				),
				array(
					'Regex',
					true,
					array(
						'/^\+[0-9]{12}/',
						'messages'	=>	array('regexNotMatch'	=>	'Fax není v platném formátu. Např.: +420123456789')
					)
				)
			)
		));
		$this->addElement('submit', 'saveEshop', array(
			'decorators'	=>	$this->buttonDecorators,
			'label'			=>	'Aktualizovat'
		));
        
    }

	public function loadDefaultDecorators()
    {
    	$this->setDecorators(
    		array(
    			'FormElements',
        		array(
        			'HtmlTag',
        			array(
        				'tag'	=>	'div',
        				'id'	=>	'form-option-eshop'
        			)
        		),
        		array('Form')
        	)
        );
    }
}