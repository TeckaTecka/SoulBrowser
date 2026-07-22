<?php
class Auth_Form_Login extends Zend_Form
{
	public $elementDecorators = array(
		'ViewHelper',
		array('Errors', array('class'	=>	'errors')),
		'Label',
		array(array('row' => 'HtmlTag'),
		array('tag' => 'div'))
	);
	
    public $buttonDecorators = array('ViewHelper');
    								 	   
	public function init()
    {
        $this->addElement('text', 'username', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Login',
        	'size'			=>	40,
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
        $this->addElement('password', 'password', array(
			'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Heslo',
        	'required'		=>	true,
        	'size'			=>	40,
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
							'stringLengthTooLong'	=>	"'%value%' je delší než %max% znaků")
						)
					)
				)
			));
        $this->addElement('submit', 'login', array('decorators'	=>	$this->buttonDecorators,
        										  	'label'			=>	'Přihlásit'));
    }

	public function loadDefaultDecorators()
    {
    	$this->setDecorators(
    		array(
    			'FormElements',
        		array(
        			'HtmlTag',
        			array(
        				'tag' => 'div',
        				'id' => 'form-auth-login'
        			)
        		),//, 'style'=>'width: 100%;')),
        		array('Form')
        	)
        );
    }
}