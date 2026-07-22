<?php
class Newsletter_Form_AddEmail extends Zend_Form
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
    								 	   
	public function init()
    {
    	$this->addElement('text', 'email', array(
    		'decorators'	=>	$this->elementDecorators,
    		//'label'			=>	'Email',
			//'description'	=>	'*',
			//'value'			=>	'Váš email',
    		//'onClick'		=>	'value=""',
    		'placeholder'	=>	'Váš email',
			'class'			=>	'tool-tip',
    		'title'			=>	'Email, na který bodou odesílány "Novinky"',
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
    					'messages'		=>	array(
    						'emailAddressDotAtom'			=>	"xxx1",
    						'emailAddressInvalid'			=>	"xxx2",
        					'emailAddressInvalidFormat'		=>	"Toto není validní email",
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
        																  		
        $this->addElement('submit', 'newsletterSave', array(
        	'decorators'	=>	$this->buttonDecorators,
        	'label'			=>	'Přihlásit'
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
        			'id'	=>	'form-newsletter-add'
        		)
        	),
        	array('Form')
        ));
    }
}