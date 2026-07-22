<?php
class Cart_Form_Addresses extends Zend_Form
{
	public $elementDecorators = array(
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
	private $personDecorators = array(
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
				'class'	=>	'person'
			)
		)
	);
	private $companyDecorators = array(
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
				'class'	=>	'company'
			)
		)
	);
    public $buttonDecorators = array('ViewHelper');
    public $radioDecorators = array(
		'ViewHelper',
		array('Label', array('separator' => '')), 
		array(
			'HtmlTag', array(
				'tag'	=>	'div',
				'class'	=>	'radions'
			)
		)
	);
	public $checkboxDecorators = array(
		'ViewHelper',
		array('Label', array('separator' => '')), 
		array(
			'HtmlTag', array(
				'tag'	=>	'div',
				'class'	=>	'checkbox'
			)
		)
	);
	
    public function init()
    {
    	/* FAKTURACNI UDAJE **********************************************************************/
    	$this->addElement('radio', 'addressType', array(
    		'decorators'	=>	$this->radioDecorators,
    		//'label'=>'Položky označené * je nutné vyplnit. Údaje prosím vyplňujte s diakritikou !',
    		'MultiOptions'	=>	array(
    			'1'		=>	'Koncový zákazník',
    			'2'		=>	'Firma'
    		),
    		'separator'		=>	'',
    		'value'			=>	1,
    	));
    	/* Koncovy zakaznik **********************************************************************/
    	$this->addElement('text', 'personTitle', array(
    		'decorators'	=>	$this->personDecorators,
    		'label'			=>	'Titul před jménem'
    	));
        
    	$this->addElement('text', 'personName', array(
        	'decorators'	=>	$this->personDecorators,
        	'label'			=>	'Jméno zákazníka',
        	'description'	=>	'*',
        	'required'		=>	true,
        	'validators'	=>	array(
        		array(
        			'NotEmpty',
        			true,
        			array('messages'	=>	array('isEmpty'		=>	'Jméno musí být vyplněno'))
        		)
        	)
        ));
		
    	$this->addElement('text', 'personSurname', array(
    		'decorators'	=>	$this->personDecorators,
    		'label'			=>	'Příjmení zákazníka',
    		'description'	=>	'*',
    		'required'		=>	true,
    		'validators'	=>	array(
    			array(
    				'NotEmpty',
    				true,
    				array('messages'	=>	array('isEmpty'		=>	'Příjmení musí být vyplněno'))
    			)
    		)
    	));
    	/*****************************************************************************************/
    	/* Firma *********************************************************************************/
    	$this->addElement('text', 'companyName', array(
    		'decorators'	=>	$this->companyDecorators,
        	'label'			=>	'Název firmy',
    		'description'	=>	'*',
        	'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'Název firmy musí být vyplněn'))
				)
			)
		));
		
    	$this->addElement('text', 'companyIC', array(
    		'decorators'	=>	$this->companyDecorators,
        	'label'			=>	'IČ',
    		'description'	=>	'*',
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
    		'decorators'	=>	$this->companyDecorators,
        	'label'			=>	'DIČ'
		));
		/*****************************************************************************************/
		$this->addElement('text', 'contactPerson', array(
			'decorators'	=>	$this->elementDecorators,
			'label'			=>	'Kontaktní osoba',
			//'description'	=>	'Nevyplňujte, je-li osoba shodná se jménem zákazníka'
		));
		
		$this->addElement('text', 'email', array(
    		'decorators'	=>	$this->elementDecorators,
    		'label'			=>	'Kontaktní email',
    		'description'	=>	'* např.: jmeno@email.cz',
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
		
		$this->addElement('text', 'phone', array(
    		'decorators'	=>	$this->elementDecorators,
	        'label'			=>	'Kontaktní telefon',
			'description'	=>	'* Formát +420xxxxxxxxx',
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
		
		$this->addElement('text', 'street', array(
        	'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Ulice',
        	'description'	=>	'*',
			'required'		=>	true,
			'validators'	=>	array(
        		array(
					'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'Ulice musí být vyplněno'))
				)
			)
		));
		
		$this->addElement('text', 'street_nr', array(
        	'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Číslo popisné',
        	'description'	=>	'*',
			'required'		=>	true,
			'validators'	=>	array(
        		array(
					'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'Číslo popisné musí být vyplněno'))
				)
			)
		));
		
		$this->addElement('text', 'city', array(
			'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Město nebo obec',
			'description'	=>	'*',
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
			'description'	=>	'* Zadávejte ve formátu xxxxx',
			'required'		=>	true,
			'validators'	=>	array(
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
				),
				array(
					'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'PSČ musí být vyplněno'))
				)
			)
		));
		
		$this->addElement('select', 'country', array(
			'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Země',
			'description'	=>	'*',
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
		
		$this->addDisplayGroup(array(
    		'addressType',
    		'personTitle',
    		'personName',
    		'personSurname',
    		'companyName',
    		'companyIC',
    		'companyDIC',
    		'contactPerson',
    		'email',
    		'phone',
    		'street',
			'street_nr',
    		'city',
    		'zip',
			'country'
    		),
    		'billingInformation'
    	);
        
        $this->getDisplayGroup('billingInformation')->setDecorators(array(
        	'FormElements',
         	array(
				'Fieldset', array('legend' => 'Fakturační údaje')
			)
		));
		/*****************************************************************************************/
		$this->addElement('checkbox', 'deliveryAddress', array(
			'decorators'	=>	$this->checkboxDecorators,
        	'label'			=>	'Dodací údaje jsou stejné s fakturačními údaji',
			'value'			=>	1,

    	));
		/* DODACI UDAJE **************************************************************************/				  			    					
    	
    	$this->addElement('radio', 'addressType2', array(
    		'decorators'	=>	$this->radioDecorators,
    		//'label'=>'Položky označené * je nutné vyplnit. Údaje prosím vyplňujte s diakritikou !',
    		'MultiOptions'	=>	array(
    			'1'		=>	'Koncový zákazník',
    			'2'		=>	'Firma'
    		),
    		'separator'		=>	'',
    		'value'			=>	1,
    	));
		/* Koncovy zakaznik **********************************************************************/
    	$this->addElement('text', 'personTitle2', array(
    		'decorators'	=>	$this->personDecorators,
    		'label'			=>	'Titul před jménem'
    	));
        
    	$this->addElement('text', 'personName2', array(
        	'decorators'	=>	$this->personDecorators,
        	'label'			=>	'Jméno zákazníka',
        	'description'	=>	'*',
        	'required'		=>	true,
        	'validators'	=>	array(
        		array(
        			'NotEmpty',
        			true,
        			array('messages'	=>	array('isEmpty'		=>	'Jméno musí být vyplněno'))
        		)
        	)
        ));
		
    	$this->addElement('text', 'personSurname2', array(
    		'decorators'	=>	$this->personDecorators,
    		'label'			=>	'Příjmení zákazníka',
    		'description'	=>	'*',
    		'required'		=>	true,
    		'validators'	=>	array(
    			array(
    				'NotEmpty',
    				true,
    				array('messages'	=>	array('isEmpty'		=>	'Příjmení musí být vyplněno'))
    			)
    		)
    	));
    	/*****************************************************************************************/
    	/* Firma *********************************************************************************/
    	$this->addElement('text', 'companyName2', array(
    		'decorators'	=>	$this->companyDecorators,
        	'label'			=>	'Název firmy',
    		'description'	=>	'*',
        	'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'Název firmy musí být vyplněn'))
				)
			)
		));
		
    	$this->addElement('text', 'companyIC2', array(
    		'decorators'	=>	$this->companyDecorators,
        	'label'			=>	'IČ',
    		'description'	=>	'*',
        	'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'IČ musí být vyplněno'))
				)
			)
		));
		
    	$this->addElement('text', 'companyDIC2', array(
    		'decorators'	=>	$this->companyDecorators,
        	'label'			=>	'DIČ'
		));
		/*****************************************************************************************/
		$this->addElement('text', 'contactPerson2', array(
			'decorators'	=>	$this->elementDecorators,
			'label'			=>	'Kontaktní osoba',
			//'description'	=>	'Nevyplňujte, je-li osoba shodná se jménem zákazníka'
		));
		
		$this->addElement('text', 'email2', array(
    		'decorators'	=>	$this->elementDecorators,
    		'label'			=>	'Kontaktní email',
    		'description'	=>	'* např.: jmeno@email.cz',
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
		
		$this->addElement('text', 'phone2', array(
    		'decorators'	=>	$this->elementDecorators,
	        'label'			=>	'Kontaktní telefon',
			'description'	=>	'* Formát +420xxxxxxxxx',
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
						'/^\+[0-9]{9}/',
						'messages'	=>	array('regexNotMatch'	=>	'Telefon není v platném formátu. Např.: +420123456789')
					)
				)
			)
		));
		
		$this->addElement('text', 'street2', array(
        	'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Ulice',
        	'description'	=>	'*',
			'required'		=>	true,
			'validators'	=>	array(
        		array(
					'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'Ulice musí být vyplněno'))
				)
			)
		));
		
		$this->addElement('text', 'street_nr2', array(
        	'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Číslo popisné',
        	'description'	=>	'*',
			'required'		=>	true,
			'validators'	=>	array(
        		array(
					'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'Číslo popisné musí být vyplněno'))
				)
			)
		));
		
		$this->addElement('text', 'city2', array(
			'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Město nebo obec',
			'description'	=>	'*',
			'required'		=>	true,
			'validators'	=>	array(
				array(
					'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'Město nebo obec musí být vyplněna'))
				)
			)
		));
		
		$this->addElement('text', 'zip2', array(
			'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'PSČ',
			'description'	=>	'* Zadávejte ve formátu xxxxx',
			'required'		=>	true,
			'validators'	=>	array(
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
				),
				array(
					'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'PSČ musí být vyplněno'))
				)
			)
		));
		
		$this->addElement('select', 'country2', array(
			'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Země',
			'description'	=>	'*',
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
    	
    	$this->addDisplayGroup(array(
    		'addressType2',
    		'personTitle2',
    		'personName2',
    		'personSurname2',
    		'companyName2',
    		'companyIC2',
    		'companyDIC2',
    		'contactPerson2',
    		'email2',
    		'phone2',
    		'street2',
    		'street_nr2',
    		'city2',
    		'zip2',
			'country2'
    		),
    		'deliveryInformation'
    	);
        
        $this->getDisplayGroup('deliveryInformation')->setDecorators(array(
        	'FormElements',
         	array(
				'Fieldset', array('legend' => 'Dodací údaje')
			)
		));
    	
    	
    	
    	
    	
		/* BUTTONS *******************************************************************************/
		$this->addElement('submit', 'prev', array(
			'decorators'	=>	$this->buttonDecorators,
        	'label'			=>	'Košík'
		));
		
		$this->addElement('submit', 'addressSubmit', array(
			'decorators'	=>	$this->buttonDecorators,
        	'label'			=>	'Pokračovat'
		));
		
		$this->addDisplayGroup(array(
			'prev',
        	'addressSubmit'),
        'buttons');
        
        $this->getDisplayGroup('buttons')->setDecorators(array(
        	'FormElements',
			array(
				array(
					'data' => 'HtmlTag'
				),
				array(
					'tag' => 'div',
					'class' => 'buttons'
				)
			)
		));
		/*****************************************************************************************/
	}

    public function loadDefaultDecorators()
    {
    	$this->setDecorators(array(
    		'FormElements',
        	array(
        		'HtmlTag',
        		array(
        			'tag'	=>	'div',
        			'id'	=>	'form-end-customer'
        		)
        	),
        	array('Form')
        ));
    }
}