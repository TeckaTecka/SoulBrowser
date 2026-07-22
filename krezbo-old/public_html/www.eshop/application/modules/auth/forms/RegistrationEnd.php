<?php
class Auth_Form_RegistrationEnd extends Zend_Form
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
        $this->addElement('password', 'password', array('decorators'	=>	$this->elementDecorators,
        												   'label'		=>	'Heslo',
                                                           'description'	=>    '6 až 20 znaků',
        												   'required'	=>	true,
        												   'rows'		=>	5,
        												   'cols'		=>	30,
        												   'validators'	=>	array(array('NotEmpty',
															  							 true,
															  							 array('messages'=>array('isEmpty'=>'Heslo musí být vyplněno'))
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
		$pass = Zend_Controller_Front::getInstance()->getRequest()->getParam('password', '');
		$this->addElement('password', 'password2', array(
			'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Heslo znovu',
		    'description'	=>    'pro ověření',
        	'required'		=>	true,
        	'validators'	=>	array(
				array(
					'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'Heslo musí být vyplněno'))
		   		),
			   array(
			   	'Identical',
				true,
				array(
					$pass,
					'messages'	=>	array('notSame'	=>	'Heslo se neshoduje'))
				)
			)
		));
		
        $this->addElement('submit', 'registrationend', array('decorators'	=>	$this->buttonDecorators,
        										  	'label'			=>	'Dokončit'));
    }

	public function loadDefaultDecorators()
    {
    	$this->setDecorators(array('FormElements',
        						   array('HtmlTag',
        						   		 array('tag' => 'div', 'id' => 'form-auth-registrationEnd')),//, 'style'=>'width: 100%;')),
        						   array('Form'),));
    }
}