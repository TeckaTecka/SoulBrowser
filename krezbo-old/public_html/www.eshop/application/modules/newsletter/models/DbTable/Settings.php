<?php
class Newsletter_Model_DbTable_Settings extends Zend_Db_Table_Abstract
{
	protected $_name = 'settings';
	protected $_primary = 'id';
	/**
     * Return array of Settings flag
     * @param	string	$flag
     * @return	array
     */
	public function getFlag($flag)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->where('flag = ?', $flag);
		$rows = $this->fetchAll($select);
		if ($rows){
			$data = array();
			for ($i=0;$i<count($rows);$i++) {
				$data[$rows[$i]['label']] = $rows[$i]['value'];
			}
			return $data;
		}
		return NULL;
	}
	
}