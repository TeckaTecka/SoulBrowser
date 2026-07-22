<?php
class QuickContact_Model_DbTable_Settings extends Zend_Db_Table_Abstract
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
	/**
     * Update Flag
     * @param	string		$flag
     * @param	string		$label
     * @param	string		$value
     * @return	void
     */
	public function updateFlag($flag, $label, $value)
	{
		$data = array(
			'value'		=>	$value
		);
		$where[] = "flag = '".$flag."'";
		$where[] = "label = '".$label."'";
		$this->update($data, $where);
	}
}