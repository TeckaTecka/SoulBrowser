<?php
class Cart_Form_UsersAddresses extends Zend_Form
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
	
    public $buttonDecorators = array(
    	'ViewHelper',
    	array(
			'HtmlTag', array(
				'tag'	=>	'div',
				'class'	=>	'buttons'
			)
		)
    );
    
	
    public function init()
    {
    	$this->addElement('select', 'usersBillingAddress', array(
			'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Fakturační údaje',
			'description'	=>	'*',
        	'size'			=>	1,
			'required'		=>	true,
			'validators'	=>	array(
				array(
					'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'Fakturační údaje musí být vybrány'))
				)
			)
		));
		$this->addElement('select', 'usersDeliveryAddress', array(
			'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Dodací údaje',
			'description'	=>	'Nevyplňujte, jsou-li údaje shodné s fakturačními',
        	'size'			=>	1			
		));
		$this->addElement('submit', 'usersAddressesSubmit', array(
			'decorators'	=>	$this->buttonDecorators,
        	'label'			=>	'Načíst'
		));
		        
        $this->addDisplayGroup(array(
    		'usersBillingAddress',
    		'usersDeliveryAddress',
    		'usersAddressesSubmit'
    		),
    		'usersAddresses'
    	);
        
        $this->getDisplayGroup('usersAddresses')->setDecorators(array(
        	'FormElements',
         	array(
				'Fieldset', array('legend' => 'Uložené údaje')
			)
		));
		
		/* BUTTONS *******************************************************************************/
				
		
		
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