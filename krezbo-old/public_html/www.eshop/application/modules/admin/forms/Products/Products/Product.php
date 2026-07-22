<?php
class Admin_Form_Products_Products_Product extends Zend_Form
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
    	$this->addElement('text', 'code', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Kód',
    		//'description'	=>	'*',
        	'title'			=>	'Kód nemusí být vyplněn.<br />'.
    							'Pokud kód nevyplníte,<br />'.
    							'vyplní se automaticky náhodným číslem<br />'.
    							'v rozmezí od "000000000" do "999999999".',
        	'class'			=>	'tool-tip',
        	//'required'		=>	true,
        	/*'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'Kód musí být vyplněn'))
				)
			)*/
		));
		
    	$this->addElement('text', 'title', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Název',
    		'description'	=>	'*',
        	'title'			=>	'Název musí být vyplněn',
        	'class'			=>	'tool-tip',
        	'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'Název musí být vyplněn'))
				)
			)
		));
													
        $this->addElement('text', 'title_menu', array(
        	'decorators'	=>	$this->elementDecorators,
	        'label'			=>	'Název v menu',
        	'title'			=>	'Název stránky, který se objevuje např. v drobečkové navigaci.<br />'.
        						'Někdy je praktičtější použít kratší název než je název stránky.<br />'.
        						'Pokud necháte políčko <b>prázdné</b>,<br />'.
        						'vyplní se <b>automaticky</b> podle názvu',
	        'class'			=>	'tool-tip'
        ));
        
		$this->addElement('text', 'title_url', array(
			'decorators'	=>	$this->elementDecorators,
	        'label'			=>	'URL adresa',
			'title'			=>	'Část adresy, na které se stránka nachází.<br />'.
								'Je důležitá pro vyhledávače.<br />'.
								'Celá adresa je vždy www.obchod.cz/url-kategorie/url_zbozi.<br />'.
								'Pokud necháte políčko <b>prázdné</b>,<br />'.
								'vyplní se <b>automaticky</b> podle názvu.',
	        'class'			=>	'tool-tip'
		));
		
		
		$this->addElement('text', 'price', array(
			'decorators'	=>	$this->elementDecorators,
	        'label'			=>	'Cena s DPH',
			'description'	=>	'*',
			'title'			=>	'Cena s DPH, za kterou chcete zboží prodávat',
	        'class'			=>	'tool-tip',
			'required'		=>	true,
			'validators'	=>	array(
				array(
					'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'Cena musí být vyplněna'))
				),
				array(
					'Digits',
					true,
					array(
						'messages'	=>	array('notDigits'	=>	'"%value%" není platná cena. Formát ceny např.: "125"')
					)
				)
			)
		));
		
		
		$this->addElement('text', 'price_orig', array(
			'decorators'	=>	$this->elementDecorators,
	        'label'			=>	'Původní cena',
			'title'			=>	'Původní cena, za kterou se prodávalo',
	        'class'			=>	'tool-tip',
			'validators'	=>	array(
				array(
					'Digits',
					true,
					array(
						'messages'	=>	array('notDigits'	=>	'"%value%" není platná cena. Formát ceny např.: "125"')
					)
				)
			)
		));
		
		$this->addElement('select', 'manufactorers', array(
    		'decorators'	=>	$this->selectDecorators,
	        'label'			=>	'Výrobce',
    		'required'		=>	true
    	));
    	
		$this->addElement('select', 'vat', array(
    		'decorators'	=>	$this->selectDecorators,
	        'label'			=>	'DPH',
    		'required'		=>	true
    	));
		
    	$this->addElement('select', 'availability', array(
    		'decorators'	=>	$this->selectDecorators,
	        'label'			=>	'Dostupnost',
    		'required'		=>	true
    	));
    	
		$this->addElement('textarea', 'short_desc', array(
			'decorators'	=>	$this->elementDecorators,
	        'label'			=>	'Stručný popis',
			'description'	=>	'*',
			'title'			=>	'Stručný popis musí být vyplněn<br />'.
								'Maximální délka textu je 120 znaků.<br />'.
								'Stručný popis je zobrazen v náhledu zboží.',
	        'class'			=>	'tool-tip',
			'required'		=>	true,
        	'validators'	=>	array(
				array(
					'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'Stručný popis musí být vyplněn'))
				),
				array(
					'StringLength',
					true,
					array(
						0,
						120,
						'UTF-8',
						'messages'	=>	array(
							'stringLengthTooLong'	=>	'Text je delší než %max% znaků.',
							'stringLengthTooShort'	=>	'x',
							'stringLengthInvalid'	=>	'xx'
						)
					)
				)
			)
		));
		
		$this->addElement('textarea', 'description', array(
			'decorators'	=>	$this->elementDecorators,
	        'label'			=>	'Popis zboží'
		));	

		$this->addElement('submit', 'saveProduct', array(
			'decorators'	=>	$this->buttonDecorators,
        	'label'			=>	'Uložit'
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
        				'id'	=>	'form-product'
        			)
        		),
        		array('Form')
        	)
        );
    }
}